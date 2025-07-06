package model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;


import dao.UserDao;
import dao.UserDaoImpl;
import dao.OrderDao;
import dao.OrderDaoImpl;
import dao.EventDao;
import dao.EventDaoImpl;
import dao.PasswordUtil;

public class Model {
	private UserDao userDao;
	private OrderDao orderDao;
	private EventDao eventDao;
	private User currentUser;
	private List<Cart> cart;
	private boolean isAdmin;

	public Model() {
		userDao = new UserDaoImpl();
		orderDao = new OrderDaoImpl();
		eventDao = new EventDaoImpl();
		this.cart = new ArrayList<>();
		this.isAdmin = false;
	}

	public void setup() throws SQLException {
		userDao.setup();
		dao.Database.setup();
		eventDao.setup();

		List<Event> initialEvents = new ArrayList<>();

		initialEvents.add(new Event("Concert", "Arena", "Mon", 50.0, 0, 200));
		initialEvents.add(new Event("Play", "Theater", "Wed", 30.0, 0, 150));
		initialEvents.add(new Event("Exhibition", "Museum", "Fri", 20.0, 0, 120));
		eventDao.addInitialEvents(initialEvents);

	}

	public UserDao getUserDao() {
		return userDao;
	}

	public OrderDao getOrderDao() {
		return orderDao;
	}

	public User getCurrentUser() {
		return this.currentUser;
	}

	public void setCurrentUser(User user) {
		currentUser = user;
	}


	public List<Cart> getCart() {
		return cart;
	}

	public void addToCart(Event event, int quantity) {

		for (Cart item : cart) {
			if (item.getEvent().equals(event)) {
				item.setQuantity(item.getQuantity() + quantity);
				return;
			}
		}

		cart.add(new Cart(event, quantity));
	}

	public void removeFromCart(Event event) {
		cart.removeIf(item -> item.getEvent().equals(event));
	}

	public void updateCartQuantity(Event event, int quantity) {
		for (Cart item : cart) {
			if (item.getEvent().equals(event)) {
				item.setQuantity(quantity);
				return;
			}
		}
	}

	public boolean validateCart() {

		DayOfWeek todayDayOfWeek = LocalDate.now().getDayOfWeek();
		int todayOrdinal = getDayOrdinal(todayDayOfWeek.toString().substring(0, 3));

		for (Cart item : cart) {

			if (!item.isAvailable()) {
				return false;
			}


			int eventOrdinal = getDayOrdinal(item.getEvent().getDay());

			if (eventOrdinal == -1) {
				return false;
			}


			if (eventOrdinal < todayOrdinal) {
				return false;
			}
		}
		return true;
	}

	public String validateMessage() {
		DayOfWeek todayDayOfWeek = LocalDate.now().getDayOfWeek();
		int todayOrdinal = getDayOrdinal(todayDayOfWeek.toString().substring(0, 3));

		for (Cart item : cart) {
			if (!item.isAvailable()) {
				return "Not enough seats available for " + item.getEvent().getEvent() +
						". Only " + item.getAvailableSeats() + " seats left.";
			}


			int eventOrdinal = getDayOrdinal(item.getEvent().getDay());
			if (eventOrdinal == -1 || eventOrdinal < todayOrdinal) {
				return "Event '" + item.getEvent().getEvent() + "' on " + item.getEvent().getDay() + " is in the past and cannot be booked.";
			}
		}
		return null;
	}

	public void clearCart() {
		cart.clear();
	}

	public boolean validateConfirmationCode(String code) {
		if (code == null || code.length() != 6) {
			return false;
		}

		for (char c : code.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}


	private int getDayOrdinal(String day) {
		if (day == null) return -1;
		String normalizedDay = day.substring(0, Math.min(day.length(), 3));
		if (normalizedDay.equalsIgnoreCase("Mon")) return 0;
		else if (normalizedDay.equalsIgnoreCase("Tue")) return 1;
		else if (normalizedDay.equalsIgnoreCase("Wed")) return 2;
		else if (normalizedDay.equalsIgnoreCase("Thu")) return 3;
		else if (normalizedDay.equalsIgnoreCase("Fri")) return 4;
		else if (normalizedDay.equalsIgnoreCase("Sat")) return 5;
		else if (normalizedDay.equalsIgnoreCase("Sun")) return 6;
		return -1;
	}


	public void placeOrder() throws SQLException {
		if (currentUser == null) {
			throw new IllegalStateException("No user logged in to place an order.");
		}


		List<OrderItem> orderItems = new ArrayList<>();
		double totalPrice = 0;
		for (Cart cartItem : cart) {
			OrderItem orderItem = new OrderItem(
					cartItem.getEvent().getEvent(),
					cartItem.getQuantity(),
					(double) cartItem.getEvent().getPrice()
			);
			orderItems.add(orderItem);
			totalPrice += orderItem.getTotalItemPrice();


			Event eventToUpdate = eventDao.getEventByDetails(cartItem.getEvent().getEvent(), cartItem.getEvent().getVenue(), cartItem.getEvent().getDay());
			if (eventToUpdate != null) {
				eventToUpdate.setSold(eventToUpdate.getSold() + cartItem.getQuantity());
				eventDao.updateEvent(eventToUpdate);
			}
		}


		String orderNumber = String.format("%04d", getNextOrderNumber());


		Order order = new Order(orderNumber, LocalDateTime.now(), orderItems, totalPrice);
		orderDao.saveOrder(order, currentUser.getId());


		clearCart();
	}

	public List<Order> getOrdersForCurrentUser() throws SQLException {
		if (currentUser == null) {
			return new ArrayList<>();
		}
		return orderDao.getOrdersByUserId(currentUser.getId());
	}

	public List<Order> getAllOrders() throws SQLException {
		return orderDao.getAllOrders();
	}

	private int getNextOrderNumber() throws SQLException {

		List<Order> allOrders = orderDao.getOrdersByUserId(currentUser.getId());
		int maxOrderNum = 0;
		for (Order order : allOrders) {
			try {
				int orderNum = Integer.parseInt(order.getOrderNumber());
				maxOrderNum = Math.max(maxOrderNum, orderNum);
			} catch (NumberFormatException e) {

			}
		}
		return maxOrderNum + 1;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}

	public boolean validateAdminLogin(String username, String password) {

		return "admin".equals(username) && "Admin321".equals(password);
	}


	public List<Event> getEvents() throws SQLException {

		return eventDao.getAllEnabledEvents();
	}

	public List<Event> getEventsForAdmin() throws SQLException {

		return eventDao.getAllEventsAdmin();
	}

	public void disableEvent(int eventId) throws SQLException {
		eventDao.updateEventDisabledStatus(eventId, true);
	}

	public void enableEvent(int eventId) throws SQLException {
		eventDao.updateEventDisabledStatus(eventId, false);
	}


	public void addEvent(Event event) throws SQLException {
		if (eventDao.eventExists(event.getEvent(), event.getVenue(), event.getDay())) {
			throw new SQLException("An event with the same name, venue, and day already exists.");
		}
		eventDao.addEvent(event);
	}

	public void deleteEvent(int eventId) throws SQLException {

		eventDao.deleteEvent(eventId);
	}

	public void modifyEvent(Event event) throws SQLException {

		Event existingEvent = eventDao.getEventByDetails(event.getEvent(), event.getVenue(), event.getDay());
		if (existingEvent != null && existingEvent.getId() != event.getId()) {
			throw new SQLException("Another event with the same name, venue, and day already exists.");
		}
		eventDao.updateEvent(event);
	}

	public boolean eventExists(String eventName, String venue, String day) throws SQLException {
		return eventDao.eventExists(eventName, venue, day);
	}

	public Event getEventByDetails(String eventName, String venue, String day) throws SQLException {
		return eventDao.getEventByDetails(eventName, venue, day);
	}

	public Event getEventById(int id) throws SQLException {
		return eventDao.getEventById(id);
	}

	public List<Event> getEventsByName(String eventName) throws SQLException {
		return eventDao.getEventsByName(eventName);
	}

	public User getUserById(int userId) throws SQLException {
		return userDao.getUserById(userId);
	}

	public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
		User user = userDao.getUserById(userId);
		if (user == null) {
			return false;
		}


		String hashedOldPassword = PasswordUtil.hashSHA1(oldPassword);

		if (!user.getPassword().equals(hashedOldPassword)) {
			return false;
		} else {
		}


		String hashedNewPassword = PasswordUtil.hashSHA1(newPassword);
		userDao.updateUserPassword(userId, hashedNewPassword);
		return true;
	}
}
