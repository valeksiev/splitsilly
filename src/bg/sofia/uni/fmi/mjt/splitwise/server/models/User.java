package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;

public class User extends Base {
	
	private String password;
	private String username;
	private List<String> groupIds = new ArrayList<String>();
	
	public User() {
	}
	
	public User(String username, String password) {
		this.setUsername(username);
		this.password = hashPassword(password);
	}
		
	@Override
	public String toString() {
		return "User [password=" + password + ", username=" + username + ", groupIds=" + groupIds + ", id=" + id + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<String> groupIds) {
		this.groupIds = groupIds;
	}

	public static User findOneBy(Predicate<Base> predicate) {
		return (User) findOneBy("user", predicate);
	}

	public static List<User> findAllBy(Predicate<Base> predicate) {
		return findAllBy("user", predicate)
				.stream()
				.map(user -> (User) user)
				.collect(Collectors.toList());
	}

	private String hashPassword(String plain) {
		if (plain == null || plain.isEmpty()) {
			return null;
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
			return convertBytesToHex(bytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unsupported hashing algorithm: SHA1; Can't hash password", e);
		}
	}

	private String convertBytesToHex(byte[] bytes) {
		StringBuilder hex = new StringBuilder();
		for (byte current : bytes) {
			hex.append(String.format("%02x", current));
		}

		return hex.toString();
	}
	
	protected boolean validate() {
		//TODO add some error messages
		if (username == null) {
			return false;
		} else if (password == null) {
			return false;
		} else {
			User other = (User) User.findOneBy((model) -> username.equals(((User) model).getUsername()));
			if (other != null) {
				return false;
			}
		}
		return true;
	}

	public boolean passwordEquals(String otherPassword) {
		if (otherPassword == null || otherPassword.isEmpty()) {
			return false;
		}
		return password.equals(hashPassword(otherPassword));
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
		this.password = hashPassword(password);
	}

	public boolean addGroup(Group group) {
		return groupIds.add(group.getId());
	}

	public Group getFriendshipWith(String otherUsername) {
		return getGroups().stream().filter(group -> group.hasUserByUsername(otherUsername)).findFirst().orElse(null);
	}

	public ArrayList<Group> getGroups() {
		return Group.findAllBy((group) -> groupIds.contains(group.getId()));
	}
	
	public ArrayList<Group> getFriendships() {
		return (ArrayList<Group>) getGroups()
		.stream()
		.filter(group -> group.isFriendship())
		.collect(Collectors.toList());	
	}
	
	public ArrayList<Group> getNonFriendships() {
		return (ArrayList<Group>) getGroups()
		.stream()
		.filter(group -> !group.isFriendship())
		.collect(Collectors.toList());	
	}

	public ArrayList<Transaction> getTransactions() {
		return Transaction.findAllBy(transaction -> ((Transaction) transaction).isUserConcerned(this));
	}

	public String getName() {
		return username;
	}
}
