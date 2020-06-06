package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;

public class Group extends Base {

	private boolean isFriendship = false;
	private List<String> userIds = new ArrayList<String>();
	private String name;

	public Group(String name) {
		this.name = name;
	}

	public Group() {
		name = "friendship";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected boolean validate() {
		// TODO Auto-generated method stub
		return true;
	}

	public static ArrayList<Group> findAllBy(Predicate<Base> predicate) {
		return (ArrayList<Group>) findAllBy("group", predicate).stream().map(group -> (Group) group)
				.collect(Collectors.toList());
	}

	public void isFriendship(boolean isFriendship) {
		this.isFriendship = isFriendship;
	}

	public boolean isFriendship() {
		return isFriendship;
	}

	public void setFriendship(boolean isFriendship) {
		this.isFriendship = isFriendship;
	}

	public boolean addUser(User user) {
		if (isFriendship && userIds.size() == 2) {
			return false;
		}
		return userIds.add(user.getId()) && user.addGroup(this);
	}

	public List<User> getUsers() {
		return User.findAllBy((user) -> userIds.contains(user.getId()));
	}

	public boolean hasUserByUsername(String otherUsername) {
		return getUsers().stream().map(user -> user.getUsername()).collect(Collectors.toList()).contains(otherUsername);
	}

	public boolean hasUser(User user) {
		return getUsers().contains(user);
	}

	public static Group findOneBy(Predicate<Base> predicate) {
		return (Group) findOneBy("group", predicate);
	}

	public void getSummaryFor(User summaryForUser, PrintWriter writer) {
		Map<String, Float> summary = new HashMap<String, Float>();
		for (User user : getUsers()) {
			if (user.getId().equals(summaryForUser.getId())) {
				continue;
			}
			summary.put(user.getUsername(), 0f);
		}
		for (Transaction transaction : getTransactions()) {
			if (transaction.getPayerId().equals(summaryForUser.getId())) {
				addToSummary(summary, transaction.getReceiver().getUsername(), -transaction.getAmount());
			}
			if (transaction.getReceiverId().equals(summaryForUser.getId())) {
				addToSummary(summary, transaction.getPayer().getUsername(), +transaction.getAmount());
			}
		}
		
		if (!isFriendship) {
			writer.println(String.format("* %s", name));
		}
		for (Entry<String, Float> entry : summary.entrySet()) {
			String label = "";
			if (entry.getValue() < 0) {
				label = "owes you";
			} else if (entry.getValue() > 0) {
				label = "you owe";
			} else {
				continue;
			}
			writer.println(String.format("- %s: %s %.2f", entry.getKey(), label, Math.abs(entry.getValue())));
		}
	}

	private void addToSummary(Map<String, Float> summary, String username, float amountPerUser) {
		float tmp = summary.get(username);
		summary.put(username, tmp + amountPerUser);
	}

	private ArrayList<Transaction> getTransactions() {
		return Transaction.findAllBy((transaction) -> id.equals(((Transaction) transaction).getPayingForGroupId()));
	}

	public int getUsersCount() {
		return userIds.size();
	}

}
