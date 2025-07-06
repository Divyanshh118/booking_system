package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.User;
import dao.PasswordUtil;

public class UserDaoImpl implements UserDao {
	private final String TABLE_NAME = "users";

	public UserDaoImpl() {
	}

	@Override
	public void setup() throws SQLException {
		try (Connection connection = Database.getConnection();
			 Statement stmt = connection.createStatement();) {
			String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"username VARCHAR(10) NOT NULL," +
					"password VARCHAR(40) NOT NULL," +
					"Preferred_name VARCHAR(30)," +
					"UNIQUE(username))";
			stmt.executeUpdate(sql);
		}
	}

	@Override
	public User getUser(String username, String password) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, username);
			stmt.setString(2, PasswordUtil.hashSHA1(password));

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					User user = new User();
					user.setId(rs.getInt("id"));
					user.setUsername(rs.getString("username"));
					user.setHashedPassword(rs.getString("password"));
					user.setPreferred_name(rs.getString("Preferred_name"));
					return user;
				}
				return null;
			}
		}
	}

	@Override
	public User getUserById(int userId) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setInt(1, userId);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					User user = new User();
					user.setId(rs.getInt("id"));
					user.setUsername(rs.getString("username"));
					user.setHashedPassword(rs.getString("password"));
					user.setPreferred_name(rs.getString("Preferred_name"));
					return user;
				}
				return null;
			}
		}
	}

	@Override
	public User createUser(String username, String password, String Preferred_name) throws SQLException {
		String sql = "INSERT INTO " + TABLE_NAME + "(username, password, Preferred_name) VALUES (?, ?, ?)";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			stmt.setString(2, PasswordUtil.hashSHA1(password));
			stmt.setString(3, Preferred_name);

			stmt.executeUpdate();


			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					User user = new User(username, password, Preferred_name);
					user.setId(rs.getInt(1));
					return user;
				}
			}
			return new User(username, password, Preferred_name);
		}
	}

	@Override
	public void updateUserPassword(int userId, String newHashedPassword) throws SQLException {
		String sql = "UPDATE " + TABLE_NAME + " SET password = ? WHERE id = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, newHashedPassword);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}
	}
}
