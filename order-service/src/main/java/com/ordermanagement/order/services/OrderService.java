package com.ordermanagement.order.services;

import com.ordermanagement.order.dto.*;
import com.ordermanagement.order.entity.OrderEntity;
import com.ordermanagement.order.entity.OrderItemEntity;
import com.ordermanagement.order.exceptions.OrderException;
import com.ordermanagement.order.client.RestClientService;
import com.ordermanagement.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final RestClientService restClientService;

    public OrderService(OrderRepository orderRepository,
                        ObjectMapper objectMapper,
                        RestClientService restClientService) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.restClientService = restClientService;
    }

    @Value("${customer.service}")
    private String customerServiceUrl;

    @Value("${product.service}")
    private String productServiceUrl;

    @Value("${payment.service}")
    private String paymentServiceUrl;


    @Transactional
    public Long createOrder(CreateOrderRequest request) {

        restClientService.get(
                customerServiceUrl + "/customers/" + request.getCustomerId(),
                Object.class
        );

        String shippingJson;
        try {
            shippingJson = objectMapper.writeValueAsString(request.getShippingAddress());//stores address as string in db
        } catch (Exception e) {
            throw new OrderException(
                    "Shipping serialization failed",
                    "SERIALIZATION_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SERIALIZATION_FAILED"
            );
        }

        OrderEntity orderEntity = buildOrderEntity(request, shippingJson);//dto->enty
        Long orderId = orderRepository.insertOrder(orderEntity);//db need entity

        BigDecimal totalAmount = BigDecimal.ZERO;//it is non primitive

        for (OrderItemRequest itemRequest : request.getItems()) {

            ProductResponse product =
                    restClientService.get(
                            productServiceUrl + "/products/" + itemRequest.getProductId(),
                            ProductResponse.class
                    );

            if (product == null || !"ACTIVE".equals(product.getStatus())) {
                throw new OrderException(
                        "Invalid product: " + itemRequest.getProductId(),
                        "PRODUCT_NOT_FOUND",
                        HttpStatus.NOT_FOUND,
                        "PRODUCT_NOT_FOUND"
                );
            }

            BigDecimal lineTotal =
                    product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItemEntity itemEntity =
                    buildOrderItemEntity(orderId, itemRequest, product, lineTotal);

            orderRepository.saveOrderItem(itemEntity);

            totalAmount = totalAmount.add(lineTotal);
        }

        orderRepository.updateTotalAmount(orderId, totalAmount);

        return orderId;
    }


    @Transactional
    public void confirmOrder(Long orderId) {

        OrderEntity order = getOrderEntity(orderId);

        validateOrderState(order, "CREATED");

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(order.getTotalAmount());
        paymentRequest.setOrderId(orderId);
        paymentRequest.setCurrency(order.getCurrency());
        paymentRequest.setMethod("UPI");
        paymentRequest.setIdempotencyKey("ORDER_" + orderId);

        PaymentResponse paymentResponse =
                restClientService.post(
                        paymentServiceUrl + "/payments",
                        paymentRequest,
                        PaymentResponse.class
                );

        if (paymentResponse == null || paymentResponse.getPaymentId() == null) {
            throw new OrderException(
                    "Payment failed",
                    "PAYMENT_FAILED",
                    HttpStatus.BAD_REQUEST,
                    "PAYMENT_FAILED"
            );
        }

        orderRepository.updatePayment(orderId, paymentResponse.getPaymentId());

        List<OrderItemEntity> items = orderRepository.findItemsByOrderId(orderId);

        for (OrderItemEntity item : items) {
            String url = productServiceUrl + "/inventory/deduct?productId="
                    + item.getProductId()
                    + "&quantity=" + item.getQuantity();

            restClientService.postVoid(url, null);
        }

        orderRepository.updateStatus(orderId, "CONFIRMED");
    }


    @Transactional
    public void cancelOrder(Long orderId) {

        OrderEntity order = getOrderEntity(orderId);

        if ("CANCELLED".equals(order.getStatus())) {
            throw new OrderException(
                    "Already cancelled",
                    "INVALID_STATE",
                    HttpStatus.BAD_REQUEST,
                    "ALREADY_CANCELLED"
            );
        }

        if ("CONFIRMED".equals(order.getStatus()) && order.getPaymentId() != null) {
            String refundUrl =
                    paymentServiceUrl + "/payments/" + order.getPaymentId() + "/refund";

            restClientService.postVoid(refundUrl, null);
        }

        List<OrderItemEntity> items = orderRepository.findItemsByOrderId(orderId);

        for (OrderItemEntity item : items) {
            String url = productServiceUrl + "/inventory/restore?productId="
                    + item.getProductId()
                    + "&quantity=" + item.getQuantity();

            restClientService.postVoid(url, null);
        }

        orderRepository.updateStatus(orderId, "CANCELLED");
    }


    public OrderResponse getOrder(Long orderId) {

        OrderEntity order = getOrderEntity(orderId);
        List<OrderItemEntity> items = orderRepository.findItemsByOrderId(orderId);

        return mapToOrderResponse(order, items);
    }



    private OrderResponse mapToOrderResponse(OrderEntity order, List<OrderItemEntity> items){//to covert as response


        Order orderDto = new Order();
        orderDto.setOrderId(order.getOrderId());
        orderDto.setCustomerId(order.getCustomerId());
        orderDto.setStatus(order.getStatus());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setCurrency(order.getCurrency());
        orderDto.setPaymentId(order.getPaymentId());
        orderDto.setShippingAddress(order.getShippingAddress());


        List<OrderItem> itemDtos = items.stream().map(item -> {
            OrderItem dto = new OrderItem();
            dto.setOrderItemId(item.getOrderItemId());
            dto.setOrderId(item.getOrderId());
            dto.setProductId(item.getProductId());
            dto.setProductNameSnapshot(item.getProductNameSnapshot());
            dto.setUnitPriceSnapshot(item.getUnitPriceSnapshot());
            dto.setQuantity(item.getQuantity());
            dto.setLineTotal(item.getLineTotal());

            return dto;
        }).toList();

        return new OrderResponse(orderDto, itemDtos);
    }

    private OrderEntity buildOrderEntity(CreateOrderRequest request, String shippingJson) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(request.getCustomerId());
        order.setStatus("CREATED");
        order.setCurrency(request.getCurrency());
        order.setPaymentId(null);
        order.setShippingAddress(shippingJson);
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    private OrderItemEntity buildOrderItemEntity(Long orderId,
                                                 OrderItemRequest request,
                                                 ProductResponse product,
                                                 BigDecimal lineTotal) {

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(orderId);
        item.setProductId(request.getProductId());
        item.setProductNameSnapshot(product.getName());
        item.setUnitPriceSnapshot(product.getPrice());
        item.setQuantity(request.getQuantity());
        item.setLineTotal(lineTotal);
        return item;
    }

    private OrderEntity getOrderEntity(Long orderId) {//we use this repeatedly so
        OrderEntity order = orderRepository.findById(orderId);

        if (order == null) {
            throw new OrderException(
                    "Order not found",
                    "ORDER_NOT_FOUND",
                    HttpStatus.NOT_FOUND,
                    "ORDER_NOT_FOUND"
            );
        }
        return order;
    }

    private void validateOrderState(OrderEntity order, String expectedState) {
        if (!expectedState.equals(order.getStatus())) {
            throw new OrderException(
                    "Invalid order state",
                    "INVALID_STATE",
                    HttpStatus.BAD_REQUEST,
                    "ORDER_STATE_INVALID"
            );
        }
    }
}