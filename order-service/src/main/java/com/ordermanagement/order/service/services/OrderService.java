package com.ordermanagement.order.service.services;

import com.ordermanagement.order.service.dto.*;
import com.ordermanagement.order.service.exceptions.*;
import com.ordermanagement.order.service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.math.BigDecimal;
import java.util.List;

@Service

public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    public OrderService(OrderRepository orderRepository,
                        ObjectMapper objectMapper,
                        RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Value("${customer.service}")
    private String customerServiceUrl;

    @Value("${product.service}")
    private String productServiceUrl;

    @Value("${payment.service}")
    private String paymentServiceUrl;
@Transactional
    public Long createOrder(CreateOrderRequest request)  {
    try{
        String url=customerServiceUrl+"/customers/"+request.getCustomerId();
        restTemplate.getForObject(url, Order.class);
    } catch (RestClientException e) {
        throw new OrderException(
                "Customer not found with id " + request.getCustomerId(),
                "CUSTOMER_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                "CUSTOMER_NOT_FOUND"
        );
    }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();//order obj
        order.setCustomerId(request.getCustomerId());
        order.setStatus("CREATED");
        order.setCurrency(request.getCurrency());
        order.setPaymentId(null);
    try {
        String shippingJson = objectMapper.writeValueAsString(request.getShippingAddress());//java object to json
        order.setShippingAddress(shippingJson);
    } catch (Exception e) {
        throw new OrderException(
                "Failed to serialize shipping address",
                "SERIALIZATION_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SERIALIZATION_FAILED"
        );
    }
        order.setTotalAmount(BigDecimal.ZERO);
        Long orderId = orderRepository.insertOrder(order);
        for (OrderItemRequest itemRequest : request.getItems()) {//bez it contains more products so loop through we
            String productUrl = productServiceUrl + "/products/" + itemRequest.getProductId();
            ProductResponse product;
try {
     product = restTemplate.getForObject(productUrl, ProductResponse.class);
}catch (RestClientException e) {
    throw new OrderException(
            "Product service unreachable for product " + itemRequest.getProductId(),
            "EXTERNAL_SERVICE_ERROR",
            HttpStatus.SERVICE_UNAVAILABLE,
            "PRODUCT_SERVICE_DOWN"
    );
}


            if (product == null || !"ACTIVE".equals(product.getStatus())) {
                throw new OrderException(
                        "Product not found or inactive: " + itemRequest.getProductId(),
                        "PRODUCT_NOT_FOUND",
                        HttpStatus.NOT_FOUND,
                        "PRODUCT_NOT_FOUND"
                );
            }

            BigDecimal price = product.getPrice();  // get real price
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductId(itemRequest.getProductId());
            item.setProductNameSnapshot(product.getName());
            item.setUnitPriceSnapshot(price);
            item.setQuantity(itemRequest.getQuantity());
            item.setLineTotal(lineTotal);
            totalAmount = totalAmount.add(lineTotal);

            orderRepository.saveOrderItem(item);

        }
    orderRepository.updateTotalAmount(orderId, totalAmount);

    return orderId;

    }
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderException(
                    "Order not found with id " + orderId,
                    "ORDER_NOT_FOUND",
                    HttpStatus.NOT_FOUND,
                    "ORDER_NOT_FOUND"
            );
        }
        if (!"CREATED".equals(order.getStatus())) {
            throw new OrderException(
                    "Only CREATED orders can be confirmed",
                    "INVALID_ORDER_STATE",
                    HttpStatus.BAD_REQUEST,
                    "ORDER_STATE_INVALID"
            );
        }
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(order.getTotalAmount());
        paymentRequest.setOrderId(orderId);
        paymentRequest.setCurrency(order.getCurrency());
        paymentRequest.setMethod("UPI");
        paymentRequest.setIdempotencyKey("ORDER_" + orderId);
        PaymentResponse paymentResponse;
        try {
            paymentResponse =
                    restTemplate.postForObject(
                            paymentServiceUrl + "/payments",
                            paymentRequest,
                            PaymentResponse.class
                    );
        }catch (RestClientException e) {
            throw new OrderException(
                    "Payment service unavailable for order " + orderId,
                    "EXTERNAL_SERVICE_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "PAYMENT_SERVICE_DOWN"
            );
        }
        if (paymentResponse == null || paymentResponse.getPaymentId() == null) {
            throw new OrderException(
                    "Payment failed for order " + orderId,
                    "PAYMENT_FAILED",
                    HttpStatus.BAD_REQUEST,
                    "PAYMENT_FAILED"
            );
        }
        orderRepository.updatePayment(orderId, paymentResponse.getPaymentId());
        for (OrderItem item : orderRepository.findItemsByOrderId(orderId)) {//loop throgh all items in our orer
            String url = productServiceUrl + "/inventory/deduct?productId="
                    + item.getProductId()
                    + "&quantity=" + item.getQuantity();//to reduce stock
            try {
                restTemplate.postForObject(url, null, Void.class);
            } catch (RestClientException e) {
                throw new OrderException(
                        "Inventory deduction failed for product " + item.getProductId(),
                        "EXTERNAL_SERVICE_ERROR",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INVENTORY_DEDUCTION_FAILED"
                );
            }
        }


            orderRepository.updateStatus(orderId, "CONFIRMED");

        }
    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderException(
                    "Order not found with id " + orderId,
                    "ORDER_NOT_FOUND",
                    HttpStatus.NOT_FOUND,
                    "ORDER_NOT_FOUND"
            );

        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new OrderException(
                    "Order already cancelled",
                    "INVALID_ORDER_STATE",
                    HttpStatus.BAD_REQUEST,
                    "ORDER_ALREADY_CANCELLED"
            );
        }

        if ("CONFIRMED".equals(order.getStatus())) {


            if (order.getPaymentId() != null) {
                try {
                    restTemplate.postForObject(
                            paymentServiceUrl + "/payments/" + order.getPaymentId() + "/refund",
                            null,
                            Void.class
                    );
                }
                catch (RestClientException e) {
                    throw new OrderException(
                            "Refund failed for payment " + order.getPaymentId(),
                            "EXTERNAL_SERVICE_ERROR",
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "REFUND_FAILED"
                    );
                }
                }
            }


            for (OrderItem item : orderRepository.findItemsByOrderId(orderId)) {//fetch items to reduce
                String url = productServiceUrl + "/inventory/restore?productId="
                        + item.getProductId()
                        + "&quantity=" + item.getQuantity();
                try {


                    restTemplate.postForObject(url, null, Void.class);
                }catch (RestClientException e) {
                    throw new OrderException(
                            "Inventory restore failed for product " + item.getProductId(),
                            "EXTERNAL_SERVICE_ERROR",
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "INVENTORY_RESTORE_FAILED"
                    );
                }

            }

            orderRepository.updateStatus(orderId, "CANCELLED");

    }
    public Order getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId);


        if (order == null) {
            throw new OrderException(
                    "Order not found with id " + orderId,
                    "ORDER_NOT_FOUND",
                    HttpStatus.NOT_FOUND,
                    "ORDER_NOT_FOUND"
            );
        }

        return order;
    }


    public List<OrderItem> getOrderItems(Long orderId) {
        return  orderRepository.findItemsByOrderId(orderId);
    }
}

