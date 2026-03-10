package com.ordermanagement.order.service.repository;

import com.ordermanagement.order.service.dto.Order;
import com.ordermanagement.order.service.dto.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbcTemplate;
    OrderRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }
    @Value("${order.insert}")
    private String insertOrderSql;
    @Value("${orderitem.insert}")
    private String insertOrderItemSql;
    @Value("${order.findById}")
    private String findByIdSql;

    @Value("${orderitem.findByOrderId}")
    private String findItemsSql;

    @Value("${order.updateStatus}")
    private String updateStatusSql;

    @Value("${order.updatePayment}")
    private String updatePaymentSql;

    @Value("${order.updateTotal}")
    private String updateTotalSql;
   
    public Long insertOrder(Order order){
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
    }
    public void saveOrderItem(OrderItem item) {

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
    public List<OrderItem> findItemsByOrderId(Long orderId) {

        return jdbcTemplate.query(
                findItemsSql,
                this::mapOrderItemRow,
                orderId
        );
    }
    public Order findById(Long orderId) {

        List<Order> orders = jdbcTemplate.query(
                findByIdSql,
                this::mapOrderRow,
                orderId
        );

        if (orders.isEmpty()) {
            return null;
        }

        return orders.get(0);
    }
    public void updateStatus(Long orderId, String status) {

        jdbcTemplate.update(
                updateStatusSql,
                status,
                orderId
        );
    }
    public void updatePayment(Long orderId, Long paymentId) {

        jdbcTemplate.update(
                updatePaymentSql,
                paymentId,
                orderId
        );
    }
    public void updateTotalAmount(Long orderId, BigDecimal totalAmount) {

        jdbcTemplate.update(
                updateTotalSql,
                totalAmount,
                orderId
        );
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
