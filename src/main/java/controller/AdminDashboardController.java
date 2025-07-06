package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Model;
import model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXMLLoader;

import java.util.Optional;

public class AdminDashboardController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private TableView<Event> eventsTable;
    @FXML
    private TableColumn<Event, String> eventColumn;
    @FXML
    private TableColumn<Event, String> venueColumn;
    @FXML
    private TableColumn<Event, String> daysColumn;
    @FXML
    private TableColumn<Event, Double> priceColumn;
    @FXML
    private TableColumn<Event, Integer> soldColumn;
    @FXML
    private TableColumn<Event, Integer> totalColumn;
    @FXML
    private TableColumn<Event, Boolean> disabledColumn;
    @FXML
    private MenuItem disableEventMenuItem;
    @FXML
    private MenuItem enableEventMenuItem;
    @FXML
    private Button logoutButton;
    @FXML
    private Button addEventButton;
    @FXML
    private Button modifyEventButton;
    @FXML
    private Button deleteEventButton;
    @FXML
    private Button viewAllOrdersButton;

    public AdminDashboardController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {

        eventColumn.setCellValueFactory(new PropertyValueFactory<>("event"));
        venueColumn.setCellValueFactory(new PropertyValueFactory<>("venue"));
        daysColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("displayPrice"));
        soldColumn.setCellValueFactory(new PropertyValueFactory<>("sold"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        disabledColumn.setCellValueFactory(new PropertyValueFactory<>("disabled"));


        loadEvents();


        logoutButton.setOnAction(event -> handleLogout());
        addEventButton.setOnAction(event -> handleAddEvent());
        modifyEventButton.setOnAction(event -> handleModifyEvent());
        deleteEventButton.setOnAction(event -> handleDeleteEvent());
        viewAllOrdersButton.setOnAction(event -> handleViewAllOrders());


        disableEventMenuItem.setOnAction(event -> handleDisableEvent());
        enableEventMenuItem.setOnAction(event -> handleEnableEvent());
    }

    private void loadEvents() {
        try {
            List<Event> events = model.getEventsForAdmin();
            ObservableList<Event> groupedEventList = FXCollections.observableArrayList();

            eventsTable.getItems().clear();

            for (Event currentEvent : events) {
                boolean found = false;
                for (Event groupedEvent : groupedEventList) {
                    if (groupedEvent.getEvent().equals(currentEvent.getEvent())) {

                        StringBuilder daysAndVenues = new StringBuilder(groupedEvent.getDay());
                        daysAndVenues.append(", ").append(currentEvent.getVenue()).append(" – ").append(currentEvent.getDay());

                        groupedEvent.dayProperty().set(daysAndVenues.toString());
                        groupedEvent.setSold(groupedEvent.getSold() + currentEvent.getSold());
                        groupedEvent.setTotal(groupedEvent.getTotal() + currentEvent.getTotal());
                        groupedEvent.setDisabled(groupedEvent.isDisabled() || currentEvent.isDisabled());


                        found = true;
                        break;
                    }
                }

                if (!found) {

                    groupedEventList.add(new Event(
                            currentEvent.getId(),
                            currentEvent.getEvent(),
                            "Multiple Venues",
                            currentEvent.getVenue() + " – " + currentEvent.getDay(),
                            currentEvent.getPrice(),
                            currentEvent.getSold(),
                            currentEvent.getTotal(),
                            currentEvent.isDisabled()
                    ));
                }
            }


            for (Event groupedEvent : groupedEventList) {
                List<Event> individualEventsInGroup = model.getEventsByName(groupedEvent.getEvent());
                boolean allPricesSame = true;
                double firstPrice = -1.0;

                if (!individualEventsInGroup.isEmpty()) {
                    firstPrice = individualEventsInGroup.get(0).getPrice();
                    for (Event eventInGroup : individualEventsInGroup) {
                        if (eventInGroup.getPrice() != firstPrice) {
                            allPricesSame = false;
                            break;
                        }
                    }
                }

                if (allPricesSame && firstPrice != -1.0) {
                    groupedEvent.setDisplayPrice(String.valueOf(firstPrice));
                } else {
                    groupedEvent.setDisplayPrice("Varies");
                }
            }

            eventsTable.setItems(groupedEventList);
            eventsTable.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading events: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddEvent() {
        try {
            openEventForm(null);
        } catch (java.io.IOException | SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error opening event form: " + e.getMessage());
        }
    }

    @FXML
    private void handleModifyEvent() {
        Event selectedGroupedEvent = eventsTable.getSelectionModel().getSelectedItem();
        if (selectedGroupedEvent != null) {
            try {
                List<Event> individualEvents = model.getEventsByName(selectedGroupedEvent.getEvent());

                if (individualEvents.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "No individual events found for this grouped event.");
                    return;
                }

                if (individualEvents.size() == 1) {
                    try {
                        openEventForm(individualEvents.get(0));
                    } catch (java.io.IOException e1) {
                        e1.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Error opening event form: " + e1.getMessage());
                    } catch (java.sql.SQLException e2) {
                        e2.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Database Error: " + e2.getMessage());
                    }
                    return;
                }

                Dialog<Event> dialog = new Dialog<>();
                dialog.setTitle("Select Event Instance");
                dialog.setHeaderText("Select the specific event instance to modify:");

                ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

                ListView<Event> eventListView = new ListView<>();
                eventListView.getItems().addAll(individualEvents);
                eventListView.setCellFactory(lv -> new ListCell<Event>() {
                    @Override
                    protected void updateItem(Event event, boolean empty) {
                        super.updateItem(event, empty);
                        setText(empty ? null : event.getEvent() + " - " + event.getVenue() + " - " + event.getDay());
                    }
                });

                dialog.getDialogPane().setContent(eventListView);

                Button selectButton = (Button) dialog.getDialogPane().lookupButton(selectButtonType);
                selectButton.setDisable(true);
                eventListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    selectButton.setDisable(newValue == null);
                });


                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == selectButtonType) {
                        return eventListView.getSelectionModel().getSelectedItem();
                    }
                    return null;
                });

                Optional<Event> result = dialog.showAndWait();

                if (result.isPresent()) {
                    Event selectedEvent = result.get();
                    try {
                        openEventForm(selectedEvent);
                    } catch (java.io.IOException | SQLException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Error opening event form: " + e.getMessage());
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error retrieving event details: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event to modify.");
        }
    }

    private void openEventForm(Event event) throws java.io.IOException, SQLException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EventFormView.fxml"));
        Stage formStage = new Stage();
        EventFormController eventFormController = new EventFormController(formStage, model, event);
        loader.setController(eventFormController);

        VBox root = loader.load();
        Scene scene = new Scene(root);
        formStage.setScene(scene);
        formStage.setTitle(event == null ? "Add New Event" : "Modify Event");
        formStage.initOwner(stage);
        formStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        formStage.showAndWait();

        loadEvents();
    }

    @FXML
    private void handleDeleteEvent() {
        Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to delete the event: " + selectedEvent.getEvent() + " - " + selectedEvent.getVenue() + " - " + selectedEvent.getDay() + "?");

            java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {

                    model.deleteEvent(selectedEvent.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Event Deleted", selectedEvent.getEvent() + " - " + selectedEvent.getVenue() + " - " + selectedEvent.getDay() + " has been deleted.");
                    loadEvents();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting event: " + e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event to delete.");
        }
    }

    @FXML
    private void handleDisableEvent() {
        Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            try {

                model.disableEvent(selectedEvent.getId());
                showAlert(Alert.AlertType.INFORMATION, "Event Disabled", selectedEvent.getEvent() + " - " + selectedEvent.getVenue() + " - " + selectedEvent.getDay() + " has been disabled.");
                loadEvents();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error disabling event: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event to disable.");
        }
    }

    @FXML
    private void handleEnableEvent() {
        Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            try {

                model.enableEvent(selectedEvent.getId());
                showAlert(Alert.AlertType.INFORMATION, "Event Enabled", selectedEvent.getEvent() + " - " + selectedEvent.getVenue() + " - " + selectedEvent.getDay() + " has been enabled.");
                loadEvents();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error enabling event: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event to enable.");
        }
    }

    @FXML
    private void handleViewAllOrders() {
        try {

            stage.hide();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminOrderHistoryView.fxml"));
            Stage orderHistoryStage = new Stage();
            AdminOrderHistoryController orderHistoryController = new AdminOrderHistoryController(stage, model);
            loader.setController(orderHistoryController);

            VBox root = loader.load();
            orderHistoryController.showStage(root);

        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading order history view: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {

        model.setAdmin(false);


        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            LoginController loginController = new LoginController(stage, model);
            loader.setController(loginController);

            Pane root = loader.load();
            Scene loginScene = new Scene(root, 500, 300);
            stage.setScene(loginScene);
            stage.setTitle("Welcome");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading login view: " + e.getMessage());
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }
}