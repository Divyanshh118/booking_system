package model;

import dao.PasswordUtil;

public class User {
	private int id;
	private String username;
	private String password;
	private String Preferred_name;




	public User() {
	}

	public User(String username, String password, String Preferred_name) {
		this.username = username;
		setPlainPassword(password);
		this.Preferred_name = Preferred_name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPlainPassword(String plainPassword) {
		this.password = PasswordUtil.hashSHA1(plainPassword);
	}

	public void setHashedPassword(String hashedPassword) {
		this.password = hashedPassword;
	}

	public String getPreferred_name() {
		return Preferred_name;
	}

	public void setPreferred_name(String Preferred_name) {
		this.Preferred_name = Preferred_name;
	}

}
