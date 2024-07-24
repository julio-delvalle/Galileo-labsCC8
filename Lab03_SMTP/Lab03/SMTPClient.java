import java.io.*;
import java.net.*;

public class SMTPClient {

	private static final String SERVER = "localhost";
	private static final int PORT = 25;

	public static void main(String[] args) {
		(new SMTPClient()).start();
	}

	public void start() {
		try (Socket socket = new Socket(SERVER, PORT);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
			String fromServer;
			int i = 0;
			while ((fromServer = in.readLine()) != null) {
				System.out.println("-> fromServer = " + fromServer);
				if (fromServer.startsWith("220")) {
					out.println("HELO DEMO");
					System.out.println("<- HELO DEMO");
				} else if (fromServer.startsWith("250")) {
					out.println("QUIT");
					System.out.println("<- QUIT");
				} else if (fromServer.equals("221")) {
					i = 0;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}