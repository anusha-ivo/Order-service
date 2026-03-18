package com.ordermanagement.order.service.services;

import com.ordermanagement.order.service.dto.*;
import com.ordermanagement.order.service.exceptions.ExternalServiceException;
import com.ordermanagement.order.service.exceptions.InvalidOrderStateException;
import com.ordermanagement.order.service.exceptions.ResourceNotFoundException;
import com.ordermanagement.order.service.exceptions.SerializationException;
import com.ordermanagement.order.service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
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
        throw new ResourceNotFoundException("Customer not found with id " + request.getCustomerId());
    }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus("CREATED");
        order.setCurrency(request.getCurrency());
        order.setPaymentId(null);
    try {
        String shippingJson = objectMapper.writeValueAsString(request.getShippingAddress());
        order.setShippingAddress(shippingJson);
    } catch (Exception e) {
        throw new SerializationException("Failed to convert shipping address");
    }
        order.setTotalAmount(BigDecimal.ZERO);
        Long orderId = orderRepository.insertOrder(order);
        for (OrderItemRequest itemRequest : request.getItems()) {//bez it contains more products so loop through we
            String productUrl = productServiceUrl + "/products/" + itemRequest.getProductId();

                ProductResponse product = restTemplate.getForObject(productUrl, ProductResponse.class);


            if (product == null || !"ACTIVE".equals(product.getStatus())) {
                throw new ResourceNotFoundException("Product not found or inactive: " + itemRequest.getProductId());
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
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }
        if (!"CREATED".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Only CREATED orders can be confirmed");
        }
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(order.getTotalAmount());
        paymentRequest.setOrderId(orderId);
        paymentRequest.setCurrency(order.getCurrency());
        paymentRequest.setMethod("UPI");
        paymentRequest.setIdempotencyKey("ORDER_" + orderId);
        PaymentResponse paymentResponse =
                restTemplate.postForObject(
                        paymentServiceUrl + "/payments",
                        paymentRequest,
                        PaymentResponse.class
                );
        if (paymentResponse == null || paymentResponse.getPaymentId() == null) {
            throw new ExternalServiceException("Payment failed for order " + orderId);
        }
        orderRepository.updatePayment(orderId, paymentResponse.getPaymentId());
        for (OrderItem item : orderRepository.findItemsByOrderId(orderId)) {
            String url = productServiceUrl + "/inventory/deduct?productId="
                        + item.getProductId()
                        + "&quantity=" + item.getQuantity();

                restTemplate.postForObject(url, null, Void.class);
            }


            orderRepository.updateStatus(orderId, "CONFIRMED");

    }
    @Transactional
    public void cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Order already cancelled");
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
                    throw new ExternalServiceException("Refund failed for payment " + order.getPaymentId());
                }
            }


            for (OrderItem item : orderRepository.findItemsByOrderId(orderId)) {
                String url = productServiceUrl + "/inventory/restore?productId="
                        + item.getProductId()
                        + "&quantity=" + item.getQuantity();
                try {


                    restTemplate.postForObject(url, null, Void.class);
                }catch (RestClientException e) {
                    throw new ExternalServiceException("Inventory restore failed for product " + item.getProductId());
                }

            }

            orderRepository.updateStatus(orderId, "CANCELLED");
        }
    }
    public Order getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId);


        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        return order;
    }


    public List<OrderItem> getOrderItems(Long orderId) {
        return  orderRepository.findItemsByOrderId(orderId);
    }
}

