import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
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
	private String logsFolder = "logs";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
	private Integer nThreadServer;



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

			Logger LOGGER = Logger.getAnonymousLogger();
			LOGGER.setUseParentHandlers(false);
			Files.createDirectories(Paths.get(logsFolder));
			FileHandler fileHandler = new FileHandler(logsFolder + File.separator + "T"+ nThreadServer + "-" + dateFormat.format((new Date()).getTime()) + ".log");
			ConsoleHandler consoleHandler = new ConsoleHandler();
			fileHandler.setFormatter(new FormatterWebServer());
			consoleHandler.setFormatter(new FormatterWebServer());
			LOGGER.addHandler(fileHandler);
			LOGGER.addHandler(consoleHandler);

			out.println("220 Welcome to SMTP Server CC8");

			String line;
			String clientName = "";
			String mailFromReceived = "";
			String rcptToReceived = "";
			ArrayList<String> rcptToReceivedArray = new ArrayList<String>();
			String dataReceived = "";
			String subjectReceived = "";
			while ((line = in.nextLine()) != null) {
				try {
					
					LOGGER.info("-> fromClient: " + line);
					if (line.startsWith("HELO")) {
						clientName = line.substring(5);
						out.println("250 HELLO " + clientName + ", pleased to meet you" );
						//if(validateServerName(clientName)){
						//}else{
						//	out.println("501 Syntax Error, " + clientName + " is not a valid server name." );
						//}
					} else if (line.startsWith("MAIL FROM:")) {
						mailFromReceived = line.split(":")[1].trim().replace("<", "").replace(">", "");
						// mailFromReceived = line.substring(line.indexOf('<')+1, line.indexOf('>'));
						out.println("250 OK MAIL FROM "+mailFromReceived);
						//if(validateServerName(clientName)){
						//}else{
						//	out.println("501 Syntax Error, " + mailFromReceived + " is not a valid mail address." );
						//}
					} else if (line.startsWith("RCPT TO:")) {
						rcptToReceived = line.split(":")[1].trim().replace("<", "").replace(">", "");

						// rcptToReceived = line.substring(line.indexOf('<')+1, line.indexOf('>'));
						out.println("250 OK RCPT TO "+rcptToReceived);
						rcptToReceivedArray.add(rcptToReceived); 
						//if(validateServerName(clientName)){
						//}else{
						//	out.println("501 Syntax Error, " + rcptToReceived + " is not a valid mail address." );
						//}
					} else if (line.equals("DATA")) {
						out.println("354 End data with <CR><LF>.<CR><LF>");
						while ((line = in.nextLine()) != null) {
							dataReceived += "\n"+line;
							if (line.equals(".")) {
								
								
								int rowID = -5;
								for (String rcptToStr : rcptToReceivedArray) {
									String mailServer = rcptToStr.split("@")[1];
									if(mailServer.equals("julio.com")){
										rowID = (new SQLiteJDBC()).insertMailToDBSimple(mailFromReceived, rcptToStr, subjectReceived, dataReceived);
									}else{
										//También lo guarda incluso si no es para mi.
										rowID = (new SQLiteJDBC()).insertMailToDBSimple(mailFromReceived, rcptToStr, subjectReceived, dataReceived);
										SMTPClient tempClient = new SMTPClient(mailServer, 25);
										boolean started = tempClient.start();
										if(started){
											LOGGER.info("Se reenvió el correo a "+mailServer+"!");
											tempClient.sendMail(mailFromReceived, rcptToStr, subjectReceived, dataReceived);
											tempClient.close();
										}else{
											LOGGER.info("No se pudo conectar a "+mailServer+"!");
										}
									}
								}
								out.println("250 OK DATA #"+rowID+" <-- ID ROW ");
								LOGGER.info("250 OK DATA #"+rowID+" <-- ID ROW ");

								//out.println("Was sent to: "+rcptToReceivedArray.toString());
								//out.println("");
								
								for (String str : dataReceived.split("\n")) {
									if(str.contains("Subject:")){
										int idx = str.indexOf("Subject:");
										subjectReceived = str.substring(idx+8);
									}
								}
								break;
							}
						}
					} else if (line.equals("QUIT")) {
						

						out.println("221 Bye " + clientName);
						LOGGER.info("QUIT ");
						clientName = "";
						mailFromReceived = "";
						rcptToReceived = "";
						dataReceived = "";
						rcptToReceivedArray.clear();
						break;
					} else {
						LOGGER.info("-> no command fromClient: " + line);
						out.println("500 Unknown command aaaaa");
					}
					
				} catch (Exception e) {
					LOGGER.info("-> ERROR! fromClient: " + line+"\n\n"+e);
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