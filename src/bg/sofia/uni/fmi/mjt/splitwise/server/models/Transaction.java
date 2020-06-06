package bg.sofia.uni.fmi.mjt.splitwise.server.models;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.activerecord.Base;

public class Transaction extends Base {

	private String payerId;
	private String receiverId;
	private String payingForGroupId;
	private float amount;
	private String description;

	public static ArrayList<Transaction> findAllBy(Predicate<Base> predicate) {
		return (ArrayList<Transaction>) findAllBy("transaction", predicate).stream()
				.map(transaction -> (Transaction) transaction).collect(Collectors.toList());
	}

	public boolean isUserConcerned(User user) {
		return payerId.equals(user.getId()) || receiverId.equals(user.getId());
	}

	public Group getGroup() {
		return Group.findOneBy((group) -> payingForGroupId.equals(group.getId()));
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPayerId() {
		return payerId;
	}

	public void setPayerId(String payerId) {
		this.payerId = payerId;
	}

	public void setPayingForGroupId(String payingForGroupId) {
		this.payingForGroupId = payingForGroupId;
	}

	public String getPayingForGroupId() {
		return payingForGroupId;
	}

	@Override
	protected boolean validate() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setPayer(User payer) {
		payerId = payer.getId();
	}

	public void setPayingFor(Group payingFor) {
		payingForGroupId = payingFor.getId();
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public float getAmount() {
		return amount;
	}

	public float getAmountPerUser() {
		return amount / getGroup().getUsersCount();
	}

	public User getPayer() {
		return User.findOneBy((user) -> payerId.equals(user.getId()));
	}

	public static boolean record(User payer, Group group, float amount, String description) {
		try {
			float amountPerUser = amount / group.getUsersCount();
			for (User receiver : group.getUsers()) {
				if (receiver.getId().equals(payer.getId())) {
					continue;
				}
				Transaction transaction = new Transaction();
				transaction.setAmount(amountPerUser);
				transaction.setPayer(payer);
				transaction.setPayingFor(group);
				transaction.setDescription(description);
				transaction.setReceiver(receiver);
				transaction.save();
				boolean transactionResult = transaction.save();
				if (transactionResult) {
					Notification notification = new Notification(payer, receiver, transaction);
					notification.save();
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void setReceiver(User user) {
		receiverId = user.getId();
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public User getReceiver() {
		return User.findOneBy((user) -> receiverId.equals(user.getId()));
	}

	public static Transaction findOneBy(Predicate<Base> predicate) {
		return (Transaction) findOneBy("transaction", predicate);
	}
	
	@Override
	public String toString() {
		return String.format("%s paid %.2f for %s because of %s", getPayer().getName(), amount, getReceiver().getName(),
				description);
	}

	public void toString(PrintWriter writer) {
		writer.print(String.format("%s paid %.2f for %s", getPayer().getName(), amount,
				getReceiver().getName()));
		if(description != null && !description.isEmpty()) {
			writer.print(String.format(" because of '%s'", description));
		}
		if(!getGroup().isFriendship()) {
			writer.print(String.format(" as part of %s", getGroup().getName()));
		}
		writer.println();
	}
}
