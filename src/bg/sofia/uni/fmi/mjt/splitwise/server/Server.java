package bg.sofia.uni.fmi.mjt.splitwise.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	final private static int PORT = 8080;

	public static void main(String[] args) {				
		Server server = new Server();
		server.makeServer();
	}
		
	public void makeServer() {
		try (ServerSocket serverSocket = makeSocket()) {
			System.out.printf("server is running on localhost:%d%n", PORT);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("A client connected to server " + socket.getInetAddress());

				ConnectionHandlerRunnable connectionHandler = new ConnectionHandlerRunnable(socket);
				new Thread(connectionHandler).start();		
			}
		} catch (IOException e) {
			System.out.println("maybe another server is running or port 8080");
		}
	}
	
	private ServerSocket makeSocket() throws IOException {
		return new ServerSocket(PORT);
	}
}
