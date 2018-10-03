package qcat;

public class User {

	private String username;
	private String password;
	private String apiKey;
	private String phone;

	public User(String username, String password, String apiKey, String phone) {
		this.username = username;
		this.password = password;
		this.apiKey = apiKey;
		this.phone = phone;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

}
