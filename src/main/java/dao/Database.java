package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	private static final String DB_URL = "jdbc:sqlite:application.db";

	public static Connection getConnection() throws SQLException {

		return DriverManager.getConnection(DB_URL);
	}

	public static void setup() throws SQLException {
		try (Connection conn = getConnection();
			 Statement stmt = conn.createStatement()) {


			String createOrdersTableSQL = "CREATE TABLE IF NOT EXISTS orders (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"order_number TEXT UNIQUE NOT NULL," +
					"user_id INTEGER NOT NULL," +
					"order_date_time TEXT NOT NULL," +
					"total_price REAL NOT NULL," +
					"FOREIGN KEY(user_id) REFERENCES users(id)" +
					");";
			stmt.execute(createOrdersTableSQL);


			String createOrderItemsTableSQL = "CREATE TABLE IF NOT EXISTS order_items (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"order_id INTEGER NOT NULL," +
					"event_name TEXT NOT NULL," +
					"quantity INTEGER NOT NULL," +
					"price_per_seat REAL NOT NULL," +
					"FOREIGN KEY(order_id) REFERENCES orders(id)" +
					");";
			stmt.execute(createOrderItemsTableSQL);


			String createEventsTableSQL = "CREATE TABLE IF NOT EXISTS events (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"event TEXT NOT NULL," +
					"venue TEXT NOT NULL," +
					"day TEXT NOT NULL," +
					"price REAL NOT NULL," +
					"sold INTEGER NOT NULL," +
					"total INTEGER NOT NULL," +
					"disabled BOOLEAN DEFAULT FALSE)";
			stmt.execute(createEventsTableSQL);
		}
	}
}
