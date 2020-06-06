package bg.sofia.uni.fmi.mjt.splitwise.server.enums;

public enum Command {
	LOGIN("login"), 
	REGISTER("register"), 
	ADD_FRIEND("add-friend"), 
	CREATE_GROUP("create-group"), 
	SPLIT("split"),
	SPLIT_GROUP("split-group"), 
	PAID("paid"), 
	PAID_GROUP("paid-group"), 
	GET_STATUS("get-status"), 
	HISTORY("history");

	private final String command;

	private Command(String command) {
		this.command = command;
	}

	public boolean equals(String other) {
		return command.equals(other);
	}
}
