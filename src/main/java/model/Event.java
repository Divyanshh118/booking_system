package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Event {
    private int id;
    private final StringProperty event;
    private final StringProperty venue;
    private final StringProperty day;
    private final DoubleProperty price;
    private final IntegerProperty sold;
    private final IntegerProperty total;
    private boolean disabled;
    private final StringProperty displayPrice;

    public Event(String event, String venue, String day, double price, int sold, int total) {
        this(-1, event, venue, day, price, sold, total, false);
    }

    public Event(int id, String event, String venue, String day, double price, int sold, int total) {
        this(id, event, venue, day, price, sold, total, false);
    }

    public Event(int id, String event, String venue, String day, double price, int sold, int total, boolean disabled) {
        this.id = id;
        this.event = new SimpleStringProperty(event);
        this.venue = new SimpleStringProperty(venue);
        this.day = new SimpleStringProperty(day);
        this.price = new SimpleDoubleProperty(price);
        this.sold = new SimpleIntegerProperty(sold);
        this.total = new SimpleIntegerProperty(total);
        this.disabled = disabled;
        this.displayPrice = new SimpleStringProperty(String.valueOf(price));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEvent() {
        return event.get();
    }

    public StringProperty eventProperty() {
        return event;
    }

    public void setEvent(String event) {
        this.event.set(event);
    }

    public String getVenue() {
        return venue.get();
    }

    public StringProperty venueProperty() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue.set(venue);
    }

    public String getDay() {
        return day.get();
    }

    public StringProperty dayProperty() {
        return day;
    }

    public void setDay(String day) {
        this.day.set(day);
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public int getSold() {
        return sold.get();
    }

    public IntegerProperty soldProperty() {
        return sold;
    }

    public int getTotal() {
        return total.get();
    }

    public IntegerProperty totalProperty() {
        return total;
    }

    public void setSold(int sold) {
        this.sold.set(sold);
    }

    public void setTotal(int total) {
        this.total.set(total);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getDisplayPrice() {
        return displayPrice.get();
    }

    public StringProperty displayPriceProperty() {
        return displayPrice;
    }

    public void setDisplayPrice(String displayPrice) {
        this.displayPrice.set(displayPrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event1 = (Event) o;

        if (this.id != -1 && event1.id != -1) {
            return this.id == event1.id;
        } else {
            return event.get().equals(event1.event.get()) &&
                    venue.get().equals(event1.venue.get()) &&
                    day.get().equals(event1.day.get());
        }
    }

    @Override
    public int hashCode() {
        if (id != -1) {
            return Integer.hashCode(id);
        } else {
            return (event.get() + venue.get() + day.get()).hashCode();
        }
    }
}
