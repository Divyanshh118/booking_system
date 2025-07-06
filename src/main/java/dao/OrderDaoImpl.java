package dao;

import model.Order;
import model.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl implements OrderDao {

    @Override
    public void saveOrder(Order order, int userId) throws SQLException {

        String orderSQL = "INSERT INTO orders(order_number, user_id, order_date_time, total_price) VALUES(?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(orderSQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, order.getOrderNumber());
            pstmt.setInt(2, userId);
            pstmt.setString(3, order.getOrderDateTime().toString());
            pstmt.setDouble(4, order.getTotalOrderPrice());
            pstmt.executeUpdate();


            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long orderId = rs.getLong(1);


                    String itemSQL = "INSERT INTO order_items(order_id, event_name, quantity, price_per_seat) VALUES(?, ?, ?, ?)";
                    try (PreparedStatement itemStmt = conn.prepareStatement(itemSQL)) {
                        for (OrderItem item : order.getItems()) {
                            itemStmt.setLong(1, orderId);
                            itemStmt.setString(2, item.getEventName());
                            itemStmt.setInt(3, item.getQuantity());
                            itemStmt.setDouble(4, item.getPricePerSeat());
                            itemStmt.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Order> getOrdersByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();


        String orderSQL = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date_time DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(orderSQL)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String orderNumber = rs.getString("order_number");
                    LocalDateTime orderDateTime = LocalDateTime.parse(rs.getString("order_date_time"));
                    double totalPrice = rs.getDouble("total_price");
                    long orderId = rs.getLong("id");
                    int retrievedUserId = rs.getInt("user_id");


                    List<OrderItem> items = new ArrayList<>();
                    String itemSQL = "SELECT * FROM order_items WHERE order_id = ?";
                    try (PreparedStatement itemStmt = conn.prepareStatement(itemSQL)) {
                        itemStmt.setLong(1, orderId);
                        try (ResultSet itemRs = itemStmt.executeQuery()) {
                            while (itemRs.next()) {
                                String eventName = itemRs.getString("event_name");
                                int quantity = itemRs.getInt("quantity");
                                double pricePerSeat = itemRs.getDouble("price_per_seat");
                                items.add(new OrderItem(eventName, quantity, pricePerSeat));
                            }
                        }
                    }

                    orders.add(new Order(orderNumber, orderDateTime, items, totalPrice, retrievedUserId));
                }
            }
        }
        return orders;
    }

    @Override
    public List<Order> getAllOrders() throws SQLException {
        List<Order> allOrders = new ArrayList<>();


        String orderSQL = "SELECT * FROM orders ORDER BY order_date_time DESC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(orderSQL)) {

            while (rs.next()) {
                String orderNumber = rs.getString("order_number");
                LocalDateTime orderDateTime = LocalDateTime.parse(rs.getString("order_date_time"));
                double totalPrice = rs.getDouble("total_price");
                long orderId = rs.getLong("id");
                int userId = rs.getInt("user_id");


                List<OrderItem> items = new ArrayList<>();
                String itemSQL = "SELECT * FROM order_items WHERE order_id = ?";
                try (Connection itemConn = Database.getConnection();
                     PreparedStatement itemStmt = itemConn.prepareStatement(itemSQL)) {
                    itemStmt.setLong(1, orderId);
                    try (ResultSet itemRs = itemStmt.executeQuery()) {
                        while (itemRs.next()) {
                            String eventName = itemRs.getString("event_name");
                            int quantity = itemRs.getInt("quantity");
                            double pricePerSeat = itemRs.getDouble("price_per_seat");
                            items.add(new OrderItem(eventName, quantity, pricePerSeat));
                        }
                    }
                }

                allOrders.add(new Order(orderNumber, orderDateTime, items, totalPrice, userId));
            }
        }
        return allOrders;
    }
}