package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;

public class Notification extends Base {
	
	private String toUserId;
	private String fromUserId;
	private String transactionId;
	private boolean seen = false;

	public Notification(User fromUser, User toUser, Transaction transaction) {
		this.toUserId = toUser.getId();
		this.fromUserId = fromUser.getId();
		this.transactionId = transaction.getId();
	}
	
	public static ArrayList<Notification> findAllBy(Predicate<Base> predicate) {
		return (ArrayList<Notification>) findAllBy("notification", predicate)
				.stream()
				.map(notification -> (Notification) notification)
				.collect(Collectors.toList());
	}

	@Override
	protected boolean validate() {
		return true;
	}


	public String getToUserId() {
		return toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

	public String getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public boolean isSeen() {
		return seen;
	}

	public void setSeen(boolean seen) {
		this.seen = seen;
	}

	public void display(PrintWriter writer) {
		Transaction transaction = getTransaction();
		writer.print(String.format("%s - paid %.2f for you.", getFromUser().getUsername(), transaction.getAmount()));
		if(!transaction.getGroup().isFriendship()) {
			writer.print(String.format(" for '%s'", transaction.getGroup().getName()));
		}
		writer.println();
		seen = true;
		this.save();
	}

	private Transaction getTransaction() {
		return Transaction.findOneBy((transaction) -> transactionId.equals(transaction.getId()));
	}

	private User getFromUser() {
		return User.findOneBy((user) -> fromUserId.equals(user.getId()));
	}
}
