package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import model.Model;
import model.Event;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;

import java.io.IOException;

import java.util.List;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import model.Cart;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import java.sql.SQLException;
import javafx.scene.control.PasswordField;

public class HomeController {
	private Model model;
	private Stage stage;
	private Stage parentStage;
	@FXML
	private MenuItem viewProfile;
	@FXML
	private MenuItem updateProfile;
	@FXML
	private MenuItem changePasswordMenuItem;
	@FXML
	private Label Hello_label;
	@FXML
	private TableView<Event> Table_view;
	@FXML
	private TableColumn<Event, String> Event_table;
	@FXML
	private TableColumn<Event, String> Venue_table;
	@FXML
	private TableColumn<Event, String> Day_table;
	@FXML
	private TableColumn<Event, Integer> Price_table;
	@FXML
	private TableColumn<Event, Integer> Sold_table;
	@FXML
	private TableColumn<Event, Integer> Total_table;


	@FXML
	private TableView<Cart> Cart_table;
	@FXML
	private TableColumn<Cart, String> CartEvent_column;
	@FXML
	private TableColumn<Cart, Integer> CartQuantity_column;
	@FXML
	private TableColumn<Cart, Double> CartPrice_column;
	@FXML
	private Button addToCartButton;
	@FXML
	private Button removeFromCartButton;
	@FXML
	private Button checkoutButton;
	@FXML
	private TextField quantityField;
	@FXML
	private Label cartMessage;


	@FXML
	private VBox paymentConfirmationPane;
	@FXML
	private TextField confirmationCodeField;
	@FXML
	private Button confirmPaymentButton;
	@FXML
	private Label paymentMessageLabel;

	@FXML
	private VBox changePasswordPane;
	@FXML
	private PasswordField currentPasswordField;
	@FXML
	private PasswordField newPasswordField;
	@FXML
	private PasswordField confirmNewPasswordField;
	@FXML
	private Button changePasswordButton;
	@FXML
	private Label changePasswordMessageLabel;

	@FXML
	private Button orderHistoryButton;


	@FXML
	private Button logoutButton;

	public HomeController(Stage parentStage, Model model) {
		this.stage = new Stage();
		this.parentStage = parentStage;
		this.model = model;
	}

	@FXML
	public void initialize() {
		// Table create for events
		if (Event_table != null) Event_table.setCellValueFactory(new PropertyValueFactory<>("event"));
		if (Venue_table != null) Venue_table.setCellValueFactory(new PropertyValueFactory<>("venue"));
		if (Day_table != null) Day_table.setCellValueFactory(new PropertyValueFactory<>("day"));
		if (Price_table != null) Price_table.setCellValueFactory(new PropertyValueFactory<>("price"));
		if (Sold_table != null) Sold_table.setCellValueFactory(new PropertyValueFactory<>("sold"));
		if (Total_table != null) Total_table.setCellValueFactory(new PropertyValueFactory<>("total"));


		if (CartEvent_column != null) CartEvent_column.setCellValueFactory(cellData ->
				new SimpleStringProperty(cellData.getValue().getEvent().getEvent()));
		if (CartQuantity_column != null) CartQuantity_column.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		if (CartPrice_column != null) CartPrice_column.setCellValueFactory(cellData ->
				new SimpleDoubleProperty(cellData.getValue().getEvent().getPrice() * cellData.getValue().getQuantity()).asObject());


		loadEventsTable();

		// Shopping cart functionality

		if (addToCartButton != null) {
			addToCartButton.setOnAction(event -> {
				Event selectedEvent = Table_view.getSelectionModel().getSelectedItem();
				if (selectedEvent != null) {
					try {
						int quantity = Integer.parseInt(quantityField.getText());
						if (quantity > 0) {
							model.addToCart(selectedEvent, quantity);
							updateCartTable();
							validateCart();
						} else {
							showCartMessage("Please enter a valid quantity", true);
						}
					} catch (NumberFormatException e) {
						showCartMessage("Please enter a valid quantity", true);
					}
				} else {
					showCartMessage("Please select an event", true);
				}
			});
		}


		if (removeFromCartButton != null) {
			removeFromCartButton.setOnAction(event -> {
				Cart selectedItem = Cart_table.getSelectionModel().getSelectedItem();
				if (selectedItem != null) {
					model.removeFromCart(selectedItem.getEvent());
					updateCartTable();
				} else {
					showCartMessage("Please select an item to remove", true);
				}
			});
		}

		if (checkoutButton != null) {
			checkoutButton.setOnAction(event -> {
				if (model.validateCart()) {

					showCartMessage("", false);
					showPaymentConfirmationPane(true);
				} else {
					showCartMessage(model.validateMessage(), true);
				}
			});
		}


		if (confirmPaymentButton != null) {
			confirmPaymentButton.setOnAction(event -> {
				String confirmationCode = confirmationCodeField.getText();
				if (model.validateConfirmationCode(confirmationCode)) {
					try {

						double finalTotalPrice = calculateTotalPrice();


						model.placeOrder();


						refreshEventsTable();

						showPaymentMessage("Payment successful! Total: $" + String.format("%.2f", finalTotalPrice), false);
						model.clearCart();
						updateCartTable();
						confirmationCodeField.clear();

					} catch (SQLException e) {
						e.printStackTrace();
						showPaymentMessage("Error saving order: " + e.getMessage(), true);
					}
				} else {
					showPaymentMessage("Invalid 6-digit confirmation code.", true);
				}
			});
		}


		if (orderHistoryButton != null) {
			orderHistoryButton.setOnAction(event -> handleViewOrders());
		}


		if (logoutButton != null) {
			logoutButton.setOnAction(event -> handleLogout());
		}

		if (changePasswordButton != null) {
			changePasswordButton.setOnAction(event -> handleChangePasswordSubmit());
		}
	}

	private void updateCartTable() {
		if (Cart_table != null) {
			ObservableList<Cart> cartItems = FXCollections.observableArrayList(model.getCart());
			Cart_table.setItems(cartItems);
		}
	}

	private void validateCart() {
		if (!model.validateCart()) {
			showCartMessage(model.validateMessage(), true);
		} else {
			showCartMessage("", false);
		}
	}

	private void showCartMessage(String message, boolean isError) {
		if (cartMessage != null) {
			cartMessage.setText(message);
			cartMessage.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
		}
	}


	private void showPaymentConfirmationPane(boolean show) {
		if (paymentConfirmationPane != null) {
			paymentConfirmationPane.setVisible(show);
			paymentConfirmationPane.setManaged(show);
		}
	}


	private void showPaymentMessage(String message, boolean isError) {
		if (paymentMessageLabel != null) {
			paymentMessageLabel.setText(message);
			paymentMessageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
		}
	}


	private double calculateTotalPrice() {
		double totalPrice = 0;
		for (Cart item : model.getCart()) {
			totalPrice += item.getQuantity() * item.getEvent().getPrice();
		}
		return totalPrice;
	}

	public void showStage(Pane root) {
		try {
			if (Hello_label != null && model != null && model.getCurrentUser() != null) {
				String preferredName = model.getCurrentUser().getPreferred_name();
				Hello_label.setText("Hello, " + preferredName);
			}
			
			// Refresh the events table
			loadEventsTable();
			
			Scene scene = new Scene(root, 1200, 800);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.setTitle("Home");
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error showing home stage: " + e.getMessage());
		}
	}

	@FXML
	private void handleViewOrders() {
		try {

			java.net.URL fxmlUrl = getClass().getResource("/view/Order-HistoryView.fxml");
			if (fxmlUrl == null) {
				System.err.println("Could not find Order-HistoryView.fxml");
				return;
			}


			OrderHistoryController orderHistoryController = new OrderHistoryController(stage, model);


			FXMLLoader loader = new FXMLLoader(fxmlUrl);
			loader.setController(orderHistoryController);


			Pane root = loader.load();


			orderHistoryController.showStage(root);
			stage.hide();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error loading Order-HistoryView.fxml: " + e.getMessage());
		}
	}

	@FXML
	private void handleLogout() {

		model.setCurrentUser(null);


		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));


			LoginController loginController = new LoginController(stage, model);
			loader.setController(loginController);

			Pane root = loader.load();


			Scene loginScene = new Scene(root, 500, 300);
			stage.setScene(loginScene);
			stage.setTitle("Welcome");


		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error loading LoginView.fxml during logout: " + e.getMessage());
		}
	}

	private void loadEventsTable() {
		try {
			List<Event> events = model.getEvents();
			if (Table_view != null) {

				ObservableList<Event> eventList = Table_view.getItems();
				if (eventList == null) {
					eventList = FXCollections.observableArrayList();
					Table_view.setItems(eventList);
				}

				eventList.setAll(events);
				Table_view.refresh();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error loading events from database: " + e.getMessage());
		}
	}

	private void refreshEventsTable() {
		loadEventsTable();
	}


	@FXML
	private void handleChangePassword() {
		boolean isVisible = !changePasswordPane.isVisible();
		changePasswordPane.setVisible(isVisible);
		changePasswordPane.setManaged(isVisible);

		showPaymentConfirmationPane(false);

		changePasswordMessageLabel.setText("");
		currentPasswordField.clear();
		newPasswordField.clear();
		confirmNewPasswordField.clear();
	}


	@FXML
	private void handleChangePasswordSubmit() {
		String currentPassword = currentPasswordField.getText();
		String newPassword = newPasswordField.getText();
		String confirmNewPassword = confirmNewPasswordField.getText();

		if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
			showChangePasswordMessage("Please fill in all fields.", true);
			return;
		}

		if (!newPassword.equals(confirmNewPassword)) {
			showChangePasswordMessage("New passwords do not match.", true);
			return;
		}



		try {
			if (model.changePassword(model.getCurrentUser().getId(), currentPassword, newPassword)) {
				showChangePasswordMessage("Password changed successfully!", false);

				currentPasswordField.clear();
				newPasswordField.clear();
				confirmNewPasswordField.clear();
			} else {
				showChangePasswordMessage("Incorrect current password.", true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showChangePasswordMessage("Error changing password: " + e.getMessage(), true);
		}
	}

	private void showChangePasswordMessage(String message, boolean isError) {
		if (changePasswordMessageLabel != null) {
			changePasswordMessageLabel.setText(message);
			changePasswordMessageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
		}
	}
}
