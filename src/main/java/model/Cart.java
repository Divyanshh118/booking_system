package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;


public class Cart {
    private Event event;
    private IntegerProperty quantity;
    private DoubleProperty totalPrice;

    public Cart(Event event, int quantity) {
        this.event = event;
        this.quantity = new SimpleIntegerProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(event.getPrice() * quantity);
    }

    public Event getEvent() {
        return event;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        this.totalPrice.set(event.getPrice() * quantity);
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }

    public DoubleProperty totalPriceProperty() {
        return totalPrice;
    }

    public boolean isAvailable() {
        return quantity.get() <= (event.getTotal() - event.getSold());
    }

    public int getAvailableSeats() {
        return event.getTotal() - event.getSold();
    }
}