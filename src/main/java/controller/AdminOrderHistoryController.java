package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Model;
import model.Order;
import model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AdminOrderHistoryController {

    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, String> orderNumberColumn;
    @FXML
    private TableColumn<Order, String> usernameColumn;
    @FXML
    private TableColumn<Order, LocalDateTime> orderDateTimeColumn;
    @FXML
    private TableColumn<Order, String> eventDetailsColumn;
    @FXML
    private TableColumn<Order, Double> totalPriceColumn;
    @FXML
    private Button backButton;

    public AdminOrderHistoryController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {

        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        orderDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("orderDateTime"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalOrderPrice"));


        eventDetailsColumn.setCellValueFactory(cellData -> {
            List<OrderItem> items = cellData.getValue().getItems();
            String details = items.stream()
                    .map(item -> item.getEventName() + " (" + item.getQuantity() + " tickets @ $" + String.format("%.2f", item.getPricePerSeat()) + " each)")
                    .collect(Collectors.joining(", "));
            return new javafx.beans.property.SimpleStringProperty(details);
        });


        usernameColumn.setCellValueFactory(cellData -> {
            try {
                int userId = cellData.getValue().getUserId();
                if (userId != -1) { // Assuming -1 is the default for unassigned userId
                    model.User user = model.getUserById(userId);
                    if (user != null) {
                        return new javafx.beans.property.SimpleStringProperty(user.getUsername());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();

            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });



        loadOrders();


        backButton.setOnAction(event -> handleBack());
    }

    private void loadOrders() {
        try {
            List<Order> orders = model.getAllOrders();
            ObservableList<Order> observableOrders = FXCollections.observableArrayList(orders);
            ordersTable.setItems(observableOrders);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading orders: " + e.getMessage());
        }
    }

    private void handleBack() {

        stage.close();
        if (parentStage != null) {
            parentStage.show();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showStage(VBox root) {
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("All User Orders");
        stage.setResizable(false);
        stage.show();
    }
}