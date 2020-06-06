package bg.sofia.uni.fmi.mjt.splitwise.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import bg.sofia.uni.fmi.mjt.splitwise.server.enums.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Transaction;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;

public class ConnectionHandlerRunnable implements Runnable {

	private Socket socket;
	private User currentUser = null;
	private PrintWriter writer = null;

	public ConnectionHandlerRunnable(Socket socket) {
		this.socket = socket;
	}

	public ConnectionHandlerRunnable() {
	}

	@Override
	public void run() {
		BufferedReader reader;
		Queue<String> tokens = null;
		while (true) {
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				tokens = new LinkedList<String>(Arrays.asList(reader.readLine().split("\\s+")));
				writer = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				throw new RuntimeException("Unable to initialise socket input/output streams", e);
			}
			String command = tokens.poll();
			if (isSigned()) {
				handleCommandForSignedUser(command, tokens);
			} else {
				handleCommandForUnsignedUser(command, tokens);
			}
		}
	}

	public void handleCommandForUnsignedUser(String command, Queue<String> tokens) {
		if (Command.REGISTER.equals(command)) {
			String username = tokens.poll();
			User user = new User(username, tokens.poll());
			if (user.save()) {
				currentUser = user;
				writer.println(String.format("Successfully registered and signed in as %s", currentUser.getUsername()));
			} else {
				writer.println(String.format("Unable to register as %s", username));
			}
		} else if (Command.LOGIN.equals(command)) {
			String username = tokens.poll();
			User user = getUserByUsername(username);
			if (user != null && user.passwordEquals(tokens.poll())) {
				currentUser = user;
				writer.println(String.format("Successfully signed in as %s", currentUser.getUsername()));
				List<Notification> notifications = Notification.findAllBy((notification) -> {
					Notification n = ((Notification) notification);
					return n.getToUserId().equals(currentUser.getId()) && !n.isSeen();
				});
				for (Notification n : notifications) {
					n.display(writer);
				}
			} else {
				writer.println(String.format("Unable to sign in as %s", username));
			}
		} else {
			writer.println("unknown command");
		}
	}

	private void handleCommandForSignedUser(String command, Queue<String> tokens) {

		if (Command.ADD_FRIEND.equals(command)) {
			User user = getUserByUsername(tokens.poll());
			Group friendship = new Group(String.format("%s-%s", currentUser.getUsername(), user.getName()));
			friendship.isFriendship(true);
			friendship.addUser(user);
			friendship.addUser(currentUser);
			if (friendship.save() && currentUser.save() && user.save()) {
				writer.write(String.format("%s was added as friend", user.getUsername()));
			} else {
				writer.write(String.format("Some thing went wrong while adding %s as friend", user.getUsername()));
			}
		} else if (Command.CREATE_GROUP.equals(command)) {
			Group group = new Group(tokens.poll());
			while (!tokens.isEmpty()) {
				group.addUser(getUserByUsername(tokens.poll()));
			}
			group.addUser(currentUser);
			if (group.save()) {
				group.getUsers().forEach(user -> user.save());
				writer.write(String.format("Group '%s' was created", group.getName()));
			} else {
				writer.write(String.format("Something went wrong while creating group '%s'", group.getName()));
			}
		} else if (Command.SPLIT.equals(command)) {
			float amount = Float.parseFloat(tokens.poll());
			Group friendship = currentUser.getFriendshipWith(tokens.poll());
			String description = String.join(" ", tokens);
			if (Transaction.record(currentUser, friendship, amount, description)) {
				writer.println(String.format("%.2f splitted for %s", amount, friendship.getName()));
			} else {
				writer.println(String.format("Something went wrong while splitting %.2f for %s", amount,
						friendship.getName()));
			}
		} else if (Command.SPLIT_GROUP.equals(command)) {
			float amount = Float.parseFloat(tokens.poll());
			String groupName = tokens.poll();
			Group group = Group.findOneBy((g) -> groupName.equals(((Group) g).getName()));
			String description = String.join(" ", tokens);
			if (Transaction.record(currentUser, group, amount, description)) {
				writer.println(String.format("%.2f splitted for %s", amount, group.getName()));
			} else {
				writer.println(
						String.format("Something went wrong while splitting %.2f for %s", amount, group.getName()));
			}
		} else if (Command.PAID.equals(command)) {
			float amount = Float.parseFloat(tokens.poll());
			String receiverUsername = tokens.poll();
			Group group = Group.findOneBy((g) -> {
				return String.format("%s-%s", receiverUsername, currentUser.getUsername()).equals(((Group) g).getName())
						|| String.format("%s-%s", currentUser.getUsername(), receiverUsername)
								.equals(((Group) g).getName());
			});
			if (recordPayment(tokens, amount, receiverUsername, group)) {
				writer.println(String.format("%.2f payed to %s", amount, receiverUsername));
			} else {
				writer.println(String.format("Something went wrong while paying %.2f to %s", amount, receiverUsername));
			}
		} else if (Command.PAID_GROUP.equals(command)) {
			float amount = Float.parseFloat(tokens.poll());
			String receiverUsername = tokens.poll();
			String groupName = tokens.poll();
			Group group = Group.findOneBy((g) -> groupName.equals(((Group) g).getName()));
			if (recordPayment(tokens, amount, receiverUsername, group)) {
				writer.println(String.format("%.2f payed to %s", amount, receiverUsername));
			} else {
				writer.println(String.format("Something went wrong while paying %.2f to %s", amount, receiverUsername));
			}
		} else if (Command.GET_STATUS.equals(command)) {
			writer.println("* Friends");
			for (Group friendship : currentUser.getFriendships()) {
				friendship.getSummaryFor(currentUser, writer);
			}
			for (Group group : currentUser.getNonFriendships()) {
				group.getSummaryFor(currentUser, writer);
			}
		} else if (Command.HISTORY.equals(command)) {
			List<Transaction> transactions = Transaction
					.findAllBy((transaction) -> ((Transaction) transaction).isUserConcerned(currentUser));
			for (Transaction t : transactions) {
				t.toString(writer);
			}
		} else {
			writer.write("unknown command");
		}
	}

	private boolean recordPayment(Queue<String> tokens, float amount, String receiverUsername, Group group) {
		String description = String.join(" ", tokens);
		User receiver = getUserByUsername(receiverUsername);
		Transaction transaction = new Transaction();
		transaction.setPayer(currentUser);
		transaction.setAmount(amount);
		transaction.setDescription(description);
		transaction.setPayingFor(group);
		transaction.setReceiver(receiver);
		boolean transactionResult = transaction.save();
		if (transactionResult) {
			Notification notification = new Notification(currentUser, receiver, transaction);
			notification.save();
			return true;
		}
		return false;
	}

	private boolean isSigned() {
		return currentUser != null;
	}

	private User getUserByUsername(String username) {
		return (User) User.findOneBy((model) -> username.equals(((User) model).getUsername()));
	}
}
