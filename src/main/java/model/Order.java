package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Order {
    private String orderNumber;
    private LocalDateTime orderDateTime;
    private List<OrderItem> items;
    private double totalOrderPrice;
    private int userId;

    public Order(String orderNumber, LocalDateTime orderDateTime, List<OrderItem> items, double totalOrderPrice) {
        this(orderNumber, orderDateTime, items, totalOrderPrice, -1);
    }

    public Order(String orderNumber, LocalDateTime orderDateTime, List<OrderItem> items, double totalOrderPrice, int userId) {
        this.orderNumber = orderNumber;
        this.orderDateTime = orderDateTime;
        this.items = items;
        this.totalOrderPrice = totalOrderPrice;
        this.userId = userId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getTotalOrderPrice() {
        return totalOrderPrice;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return orderDateTime.format(formatter);
    }


    public String getFormattedEventDetails() {
        StringBuilder details = new StringBuilder();
        for (OrderItem item : items) {
            if (details.length() > 0) {
                details.append(", ");
            }
            details.append(item.getEventName())
                    .append(" (")
                    .append(item.getQuantity())
                    .append(" seats)");
        }
        return details.toString();
    }
}