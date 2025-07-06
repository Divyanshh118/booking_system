package model;

public class OrderItem {
    private String eventName;
    private int quantity;
    private double pricePerSeat;

    public OrderItem(String eventName, int quantity, double pricePerSeat) {
        this.eventName = eventName;
        this.quantity = quantity;
        this.pricePerSeat = pricePerSeat;
    }

    public String getEventName() {
        return eventName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerSeat() {
        return pricePerSeat;
    }

    public double getTotalItemPrice() {
        return quantity * pricePerSeat;
    }
}