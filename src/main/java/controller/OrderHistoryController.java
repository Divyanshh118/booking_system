package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.Model;
import model.Order;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.List;

public class OrderHistoryController {
    private Model model;
    private Stage stage;
    private Stage parentStage;

    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, String> orderNumberColumn;
    @FXML
    private TableColumn<Order, String> dateColumn;
    @FXML
    private TableColumn<Order, Double> totalPriceColumn;
    @FXML
    private Button backButton;
    @FXML
    private Button exportOrdersButton;

    public OrderHistoryController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {

        if (orderNumberColumn != null) {
            orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        }
        if (dateColumn != null) {
            dateColumn.setCellValueFactory(cellData -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return new SimpleStringProperty(
                        cellData.getValue().getOrderDateTime().format(formatter)
                );
            });
        }
        if (totalPriceColumn != null) {
            totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalOrderPrice"));
        }


        loadOrderHistory();


        if (backButton != null) {
            backButton.setOnAction(event -> handleBackButton());
        }


        if (exportOrdersButton != null) {
            exportOrdersButton.setOnAction(event -> handleExportOrders());
        }
    }

    private void loadOrderHistory() {
        try {
            List<Order> orders = model.getOrdersForCurrentUser();
            if (orders.isEmpty()) {

            } else {
                ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
                if (orderTable != null) {
                    orderTable.setItems(orderList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    @FXML
    private void handleExportOrders() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Orders to Text File");
        fileChooser.setInitialFileName("orders.txt");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                List<Order> orders = model.getOrdersForCurrentUser();
                if (orders.isEmpty()) {
                    writer.write("No orders found for the current user.\n");
                } else {
                    for (Order order : orders) {
                        writer.write(String.format("Order No.: %s | Date & Time: %s | Events: %s | Total Price: $%.2f\n",
                                order.getOrderNumber(),
                                order.getFormattedDateTime(),
                                order.getFormattedEventDetails(),
                                order.getTotalOrderPrice()));
                    }
                }
                showAlert(AlertType.INFORMATION, "Export Successful", "Orders exported successfully to: " + file.getAbsolutePath());
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Database Error", "Error retrieving orders from database: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "File Export Error", "Error saving orders to file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleBackButton() {
        stage.close();
        parentStage.show();
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Order History");
        stage.show();
    }
}