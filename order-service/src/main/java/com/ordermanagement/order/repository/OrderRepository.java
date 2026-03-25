package com.ordermanagement.order.repository;

import com.ordermanagement.order.config.SqlQueryProvider;
import com.ordermanagement.order.entity.OrderEntity;
import com.ordermanagement.order.entity.OrderItemEntity;
import com.ordermanagement.order.exceptions.OrderException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SqlQueryProvider sqlQueryProvider;

    OrderRepository(JdbcTemplate jdbcTemplate, SqlQueryProvider sqlQueryProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueryProvider = sqlQueryProvider;
    }


    public Long insertOrder(OrderEntity order) {

        try {
            String sql = sqlQueryProvider.getQuery("order.insert");

            return jdbcTemplate.queryForObject(
                    sql,
                    Long.class,
                    order.getCustomerId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getCurrency(),
                    order.getPaymentId(),
                    order.getShippingAddress()
            );

        } catch (Exception e) {
            throw new OrderException("Failed to insert order",
                    "ORDER_INSERT_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_ERROR");
        }
    }


    public void saveOrderItem(OrderItemEntity item) {

        try {
            String sql = sqlQueryProvider.getQuery("orderitem.insert"); // FIXED

            jdbcTemplate.update(
                    sql,
                    item.getOrderId(),
                    item.getProductId(),
                    item.getProductNameSnapshot(),
                    item.getUnitPriceSnapshot(),
                    item.getQuantity(),
                    item.getLineTotal()
            );

        } catch (Exception e) {
            throw new OrderException("Failed to save order item",
                    "ORDERITEM_INSERT_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_ERROR");
        }
    }


    public List<OrderItemEntity> findItemsByOrderId(Long orderId) {

        String sql = sqlQueryProvider.getQuery("orderitem.findByOrderId");

        try {
            return jdbcTemplate.query(sql, this::mapOrderItemRow, orderId);

        } catch (Exception e) {
            throw new OrderException("Failed to retrieve order items",
                    "ORDERITEM_FETCH_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_ERROR");
        }
    }


    public OrderEntity findById(Long orderId) {

        String sql = sqlQueryProvider.getQuery("order.findById");

        try {
            List<OrderEntity> orders =
                    jdbcTemplate.query(sql, this::mapOrderRow, orderId);

            return orders.isEmpty() ? null : orders.get(0);

        } catch (Exception e) {
            throw new OrderException("Failed to retrieve order",
                    "ORDER_FETCH_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_ERROR");
        }
    }


    public void updateStatus(Long orderId, String status) {

        try {
            String sql = sqlQueryProvider.getQuery("order.updateStatus");

            jdbcTemplate.update(sql, status, orderId);

        } catch (Exception e) {
            throw new OrderException("Failed to update order status",
                    "ORDER_UPDATE_STATUS_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_ERROR");
        }
    }


    public void updatePayment(Long orderId, Long paymentId) {

        try {
            String sql = sqlQueryProvider.getQuery("order.updatePayment");

            jdbcTemplate.update(sql, paymentId, orderId);

        } catch (Exception e) {
            throw new OrderException("Failed to update order payment",
                    "ORDER_UPDATE_PAYMENT_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_ERROR");
        }
    }


    public void updateTotalAmount(Long orderId, BigDecimal totalAmount) {

        try {
            String sql = sqlQueryProvider.getQuery("order.updateTotal"); // FIXED

            jdbcTemplate.update(sql, totalAmount, orderId);

        } catch (Exception e) {
            throw new OrderException("Failed to update total amount",
                    "ORDER_UPDATE_TOTAL_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "DB_ERROR");
        }
    }


    private OrderItemEntity mapOrderItemRow(ResultSet rs, int rowNum) throws SQLException {

        OrderItemEntity item = new OrderItemEntity(); // FIXED

        item.setOrderItemId(rs.getLong("order_item_id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductNameSnapshot(rs.getString("product_name_snapshot"));
        item.setUnitPriceSnapshot(rs.getBigDecimal("unit_price_snapshot"));
        item.setQuantity(rs.getInt("quantity"));
        item.setLineTotal(rs.getBigDecimal("line_total"));

        return item;
    }


    private OrderEntity mapOrderRow(ResultSet rs, int rowNum) throws SQLException {

        OrderEntity order = new OrderEntity(); // entity objevt

        order.setOrderId(rs.getLong("order_id"));
        order.setCustomerId(rs.getLong("customer_id"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCurrency(rs.getString("currency"));
        order.setPaymentId(rs.getObject("payment_id", Long.class));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return order;
    }
}