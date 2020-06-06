package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionRunnable implements Runnable {

	private Socket socket;

	public ConnectionRunnable(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (true) {
				String line = reader.readLine();
				if (socket.isClosed() || line == null) {
					System.out.println("client socket is closed, stop waiting for server messages");
					return;
				}
				System.out.println(line);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
