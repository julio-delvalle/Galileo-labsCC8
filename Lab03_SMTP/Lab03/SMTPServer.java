import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.*;

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
	private Scanner in;
	private PrintWriter out;



	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]+$", Pattern.CASE_INSENSITIVE);
	public static final Pattern VALID_SERVER_NAME_REGEX = Pattern.compile("[A-Z0-9.-]+\\.[A-Z]+$", Pattern.CASE_INSENSITIVE);

	public static boolean validateEmail(String emailStr) {
			Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
			return matcher.matches();
	}
	public static boolean validateServerName(String nameStr) {
			Matcher matcher = VALID_SERVER_NAME_REGEX.matcher(nameStr);
			return matcher.matches();
	}



	public SMTPClientHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			in = new Scanner(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println("220 Welcome to SMTP Server CC8");

			String line;
			String clientName = "";
			String mailFromReceived = "";
			String rcptToReceived = "";
			ArrayList<String> rcptToReceivedArray = new ArrayList<String>();
			String dataReceived = "";
			while ((line = in.nextLine()) != null) {
				try {
					
					System.out.println("-> fromClient: " + line);
					if (line.startsWith("HELO")) {
						clientName = line.substring(5);
						if(validateServerName(clientName)){
							out.println("250 HELLO " + clientName + ", pleased to meet you" );
						}else{
							out.println("501 Syntax Error, " + clientName + " is not a valid server name." );
						}
					} else if (line.startsWith("MAIL FROM:")) {
						mailFromReceived = line.substring(line.indexOf('<')+1, line.indexOf('>'));
						if(validateServerName(clientName)){
							out.println("250 OK MAIL FROM "+mailFromReceived);
						}else{
							out.println("501 Syntax Error, " + mailFromReceived + " is not a valid mail address." );
						}
					} else if (line.startsWith("RCPT TO:")) {
						rcptToReceived = line.substring(line.indexOf('<')+1, line.indexOf('>'));
						if(validateServerName(clientName)){
							out.println("250 OK RCPT TO "+rcptToReceived);
							rcptToReceivedArray.add(rcptToReceived); 
						}else{
							out.println("501 Syntax Error, " + rcptToReceived + " is not a valid mail address." );
						}
					} else if (line.equals("DATA")) {
						out.println("354 End data with <CR><LF>.<CR><LF>");
						while ((line = in.nextLine()) != null) {
							dataReceived += "\n"+line;
							if (line.equals(".")) {
								out.println("250 OK DATA #1234 <-- ID ROW ");
								out.println("Will be sent to: "+rcptToReceivedArray.toString());
								break;
							}
							System.out.println("Email data: " + dataReceived);
						}
					} else if (line.equals("QUIT")) {
						out.println("221 Bye " + clientName);
						clientName = "";
						mailFromReceived = "";
						rcptToReceived = "";
						dataReceived = "";
						rcptToReceivedArray.clear();
						break;
					} else {
						out.println("500 Unknown command");
					}
					
				} catch (Exception e) {
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