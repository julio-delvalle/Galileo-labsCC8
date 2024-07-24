import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class SMTPServer {
	private static final int PORT = 25;

	public static void main(String[] args) {
		(new SMTPServer()).start();
	}

	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("SMTP Server is running...");
			while (true) {new SMTPClientHandler(serverSocket.accept()).start();}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class SMTPClientHandler extends Thread {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	public SMTPClientHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println("220 Welcome to SMTP Server CC8");

			String line;
			String clientName = "";
			while ((line = in.readLine()) != null) {
				System.out.println("-> fromClient: " + line);
				if (line.startsWith("HELO")) {
					clientName = line.substring(5);
					out.println("250 HELLO " + clientName + ", pleased to meet you" );
				} else if (line.startsWith("MAIL FROM:")) {
					out.println("250 OK MAIL FROM");
				} else if (line.startsWith("RCPT TO:")) {
					out.println("250 OK RCPT TO");
				} else if (line.equals("DATA")) {
					out.println("354 End data with <CR><LF>.<CR><LF>");
					while ((line = in.readLine()) != null) {
						if (line.equals(".")) {
							out.println("250 OK DATA #1234 <-- ID ROW ");
							break;
						}
						System.out.println("Email data: " + line);
					}
				} else if (line.equals("QUIT")) {
					out.println("221 Bye " + clientName);
					clientName = "";
					break;
				} else {
					out.println("500 Unknown command");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}