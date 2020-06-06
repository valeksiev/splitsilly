package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Client {
	
	private String host = "localhost";
	private int port = 8080;
	
	private PrintWriter writer;
	private Socket socket;
	private boolean connected = false;

	public Client(Queue<String> args) {
		if (!args.isEmpty()) {
			this.host = args.poll();
		}
		if (!args.isEmpty()) {
			this.port = Integer.parseInt(args.poll());
		}
	}

	public static void main(String[] args) throws IOException {
		new Client(new LinkedList<String>(Arrays.asList(args))).run();
	}

	public void run() throws IOException {
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				String command = scanner.nextLine();				
				if (command.startsWith("login") || command.startsWith("register")) {
					ensureSocket();
					executeCommand(command);
					ConnectionRunnable connectionRunnable = new ConnectionRunnable(socket);
					new Thread(connectionRunnable).start();
					connected = true;
				} else { // a server command is received
					if (connected ) {
						writer.println(command);
					} else {
						System.out.println("unknown command");
					}
				}

			}
		}
	}
	
	private void executeCommand(String command) {
		writer.println(command);
	}

	private void ensureSocket() {
		if (socket != null) {
			return;
		}
		try {
			socket = new Socket(host, port);
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("=> cannot connect to server on localhost:8080, make sure that the server is started");
		}
	}
}
