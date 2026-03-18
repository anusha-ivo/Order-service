package com.ordermanagement.order.service.repository;

import com.ordermanagement.order.service.config.SqlQueryProvider;
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
    private final SqlQueryProvider sqlQueryProvider;
    OrderRepository(JdbcTemplate jdbcTemplate,SqlQueryProvider sqlQueryProvider){
        this.jdbcTemplate=jdbcTemplate;
        this.sqlQueryProvider=sqlQueryProvider;
    }

    public Long insertOrder(Order order){
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
    }
    public void saveOrderItem(OrderItem item) {
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
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        String findItemsSql =
                sqlQueryProvider.getQuery("orderitem.findByOrderId");
        return jdbcTemplate.query(
                findItemsSql,
                this::mapOrderItemRow,
                orderId
        );
    }
    public Order findById(Long orderId) {
        String  findByIdSql =
                sqlQueryProvider.getQuery("order.findById");
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

        String updateStatusSql =
                sqlQueryProvider.getQuery("order.updateStatus");
                sqlQueryProvider.getQuery("order.updatePayment");
        jdbcTemplate.update(
                updateStatusSql,
                status,
                orderId
        );
    }
    public void updatePayment(Long orderId, Long paymentId) {

        String updatePaymentSql =
                sqlQueryProvider.getQuery("order.updatePayment");
        jdbcTemplate.update(
                updatePaymentSql,
                paymentId,
                orderId
        );
    }
    public void updateTotalAmount(Long orderId, BigDecimal totalAmount) {
        String updateTotalSql =
                sqlQueryProvider.getQuery("order.updateStatus");
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
