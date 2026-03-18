package com.ordermanagement.order.service.repository;

import com.ordermanagement.order.service.config.SqlQueryProvider;
import com.ordermanagement.order.service.dto.Order;
import com.ordermanagement.order.service.dto.OrderItem;
import com.ordermanagement.order.service.exceptions.OrderException;
import org.springframework.beans.factory.annotation.Value;
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
    OrderRepository(JdbcTemplate jdbcTemplate,SqlQueryProvider sqlQueryProvider){
        this.jdbcTemplate=jdbcTemplate;
        this.sqlQueryProvider=sqlQueryProvider;
    }

    public Long insertOrder(Order order){
        try {
            String insertOrderSql = sqlQueryProvider.getQuery("order.insert");
            return jdbcTemplate.queryForObject(
                    insertOrderSql,
                    Long.class,
                    order.getCustomerId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getCurrency(),
                    order.getPaymentId(),
                    order.getShippingAddress()
            );
        }catch (Exception e) {
            throw new OrderException("Failed to insert order", "ORDER_INSERT_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR");
        }
    }
    public void saveOrderItem(OrderItem item) {
        try {
            String insertOrderItemSql = sqlQueryProvider.getQuery("order.insert");
            jdbcTemplate.update(
                    insertOrderItemSql,
                    item.getOrderId(),
                    item.getProductId(),
                    item.getProductNameSnapshot(),
                    item.getUnitPriceSnapshot(),
                    item.getQuantity(),
                    item.getLineTotal()
            );
        }
        catch (Exception e) {
            throw new OrderException("Failed to save order item", "ORDERITEM_INSERT_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR");
        }

    }
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        String findItemsSql =
                sqlQueryProvider.getQuery("orderitem.findByOrderId");
        try {
            return jdbcTemplate.query(
                    findItemsSql,
                    this::mapOrderItemRow,
                    orderId
            );
        }catch (Exception e) {
            throw new OrderException("Failed to retrieve order items", "ORDERITEM_FETCH_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR");
        }
    }
    public Order findById(Long orderId) {
        String findByIdSql =
                sqlQueryProvider.getQuery("order.findById");
        try {
            List<Order> orders = jdbcTemplate.query(
                    findByIdSql,
                    this::mapOrderRow,
                    orderId
            );

            if (orders.isEmpty()) {
                return null;
            }

            return orders.get(0);
        }catch (Exception e) {
            if (e instanceof OrderException) throw e;
            throw new OrderException("Failed to retrieve order", "ORDER_FETCH_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR");
        }

    }
    public void updateStatus(Long orderId, String status) {

        String updateStatusSql =
                sqlQueryProvider.getQuery("order.updateStatus");
                sqlQueryProvider.getQuery("order.updatePayment");
                try {
                    jdbcTemplate.update(
                            updateStatusSql,
                            status,
                            orderId
                    );
                }catch (Exception e) {
                    throw new OrderException("Failed to update order status", "ORDER_UPDATE_STATUS_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR");
                }
    }
    public void updatePayment(Long orderId, Long paymentId) {

        String updatePaymentSql =
                sqlQueryProvider.getQuery("order.updatePayment");
        try {
            jdbcTemplate.update(
                    updatePaymentSql,
                    paymentId,
                    orderId
            );
        }catch (Exception e) {
            throw new OrderException("Failed to update order payment", "ORDER_UPDATE_PAYMENT_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR");
        }
    }
    public void updateTotalAmount(Long orderId, BigDecimal totalAmount) {
        String updateTotalSql =
                sqlQueryProvider.getQuery("order.updateStatus");
        try {
            jdbcTemplate.update(
                    updateTotalSql,
                    totalAmount,
                    orderId
            );
        }catch (Exception e) {
            throw new OrderException("Failed to update total amount", "ORDER_UPDATE_TOTAL_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR");
        }
    }
    private OrderItem mapOrderItemRow(ResultSet rs, int rowNum) throws SQLException {

        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getLong("order_item_id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductNameSnapshot(rs.getString("product_name_snapshot"));
        item.setUnitPriceSnapshot(rs.getBigDecimal("unit_price_snapshot"));
        item.setQuantity(rs.getInt("quantity"));
        item.setLineTotal(rs.getBigDecimal("line_total"));

        return item;
    }

    private Order mapOrderRow(ResultSet rs, int rowNum) throws SQLException {

        Order order = new Order();
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
