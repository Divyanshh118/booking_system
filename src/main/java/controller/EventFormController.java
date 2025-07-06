package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Event;
import model.Model;

import java.sql.SQLException;

public class EventFormController {

    @FXML
    private TextField eventNameField;
    @FXML
    private TextField venueField;
    @FXML
    private TextField dayField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField totalCapacityField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private Model model;
    private Event event;

    public EventFormController(Stage dialogStage, Model model, Event event) {
        this.dialogStage = dialogStage;
        this.model = model;
        this.event = event;
    }

    @FXML
    public void initialize() {

        if (event != null) {
            eventNameField.setText(event.getEvent());
            venueField.setText(event.getVenue());
            dayField.setText(event.getDay());
            priceField.setText(String.valueOf(event.getPrice()));
            totalCapacityField.setText(String.valueOf(event.getTotal()));
        }

        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    private void handleSave() {
        String eventName = eventNameField.getText();
        String venue = venueField.getText();
        String day = dayField.getText();
        String priceText = priceField.getText();
        String totalCapacityText = totalCapacityField.getText();


        if (eventName.isEmpty() || venue.isEmpty() || day.isEmpty() || priceText.isEmpty() || totalCapacityText.isEmpty()) {
            messageLabel.setText("All fields must be filled.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int totalCapacity = Integer.parseInt(totalCapacityText);

            if (price <= 0 || totalCapacity <= 0) {
                messageLabel.setText("Price and total capacity must be positive numbers.");
                return;
            }


            if (event != null && totalCapacity < event.getSold()) {
                messageLabel.setText("New total capacity cannot be less than sold tickets (" + event.getSold() + ").");
                return;
            }

            if (event == null) {

                Event newEvent = new Event(eventName, venue, day, price, 0, totalCapacity);
                model.addEvent(newEvent);
                messageLabel.setText("Event added successfully!");
                dialogStage.close();
            } else {

                event.setEvent(eventName);
                event.setVenue(venue);
                event.setDay(day);
                event.setPrice(price);
                event.setTotal(totalCapacity);
                model.modifyEvent(event);
                messageLabel.setText("Event modified successfully!");
                dialogStage.close();
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("Price and Total Capacity must be valid numbers.");
        } catch (SQLException e) {
            messageLabel.setText("Database Error: " + e.getMessage());
        }
    }

    private void handleCancel() {
        dialogStage.close();
    }
}