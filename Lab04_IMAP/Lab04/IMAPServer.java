import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;




public class IMAPServer {

	private static final int PORT = 143; // Puerto IMAP
	private static Map<String, String> users = new HashMap<>(); // Usuarios y contraseñas

	public static void main(String[] args) {
		// Agregar un usuario para la autenticación
		users.put("jorge@lab03.com", "admin"); // Esto debe ser del SQLite

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Servidor IMAP iniciado en el puerto " + PORT);

			while (true) {
				new ClientHandler(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ClientHandler extends Thread {
		


		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String logsFolder = "logs";
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
		private Integer nThreadServer;


		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {

				// DEFINICION DE LOGGER:
				Logger LOGGER = Logger.getAnonymousLogger();
				LOGGER.setUseParentHandlers(false);
				Files.createDirectories(Paths.get(logsFolder));
				FileHandler fileHandler = new FileHandler(logsFolder + File.separator + "T"+ nThreadServer + "-" + dateFormat.format((new Date()).getTime()) + ".log");
				ConsoleHandler consoleHandler = new ConsoleHandler();
				fileHandler.setFormatter(new FormatterWebServer());
				consoleHandler.setFormatter(new FormatterWebServer());
				LOGGER.addHandler(fileHandler);
				LOGGER.addHandler(consoleHandler);


				//DEFINICION DE BUFFERS
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				

				// Enviar mensaje de bienvenida
				out.println("* OK [CAPABILITY IMAP4rev1] IMAP Server Ready");
				LOGGER.info("< * OK [CAPABILITY IMAP4rev1] IMAP Server Ready");

				String inputLine;
				boolean authenticated = false;
				String loggedUser = "";

				while ((inputLine = in.readLine()) != null) {
					
					//Linea recibida:
					LOGGER.info("> " + inputLine );


					// ------------------- LOGIN -------------------- 
					if ( inputLine.matches("(?i)\\d+ login \"[^\"]+\" \"[^\"]+\"") ) {
						//Matches: NUM login "asdfasdf" "admin"
						String[] parts = inputLine.split(" ");

						
						if(parts[3].replace("\"","").equals("admin")){
							LOGGER.info("< " + parts[0] + " OK LOGIN completed" );
							authenticated = true;
							loggedUser = parts[2].replace("\"","");
							out.println(parts[0] + " OK LOGIN completed" );
						}else{
							LOGGER.info("< " + parts[0] + " NO LOGIN incorrect password" );
							out.println(parts[0] + " NO LOGIN incorrect password" );
						}
						
					} 


					// ------------------- SELECT -------------------- 
					else if ( inputLine.matches("(?i)\\d+ select \"[^\"]+\"") ) {
						//Matches: NUM select "ABCD"
						String[] parts = inputLine.split(" ");
						
						String requestedFolder = parts[2].replace("\"","");

						if(requestedFolder.equals("INBOX")){
							HashMap inboxInfo = (new SQLiteJDBC()).getInboxInfo(loggedUser);
							if(inboxInfo.containsKey("exists") && inboxInfo.containsKey("recent")){
								LOGGER.info("< " + "* "+inboxInfo.get("exists")+" EXISTS" );
								out.println("* "+inboxInfo.get("exists")+" EXISTS" );
								// out.println("* 2 EXISTS" );
								// LOGGER.info("* 2 EXISTS" );
								
								LOGGER.info("< " + "* "+inboxInfo.get("recent")+" RECENT" );
								out.println("* "+inboxInfo.get("recent")+" RECENT" );
								// out.println("* 1 RECENT" );
								// LOGGER.info("* 1 RECENT" );

								out.println("*" + " OK [UNSEEN "+inboxInfo.get("firstunseen")+"] First unseen.");
								LOGGER.info("< *" + " OK [UNSEEN "+inboxInfo.get("firstunseen")+"] First unseen.");
								// out.println(parts[0] + " OK [UNSEEN 2] First unseen.");


								// LOGGER.info("< " + parts[0] + " OK [UIDVALIDITY 3857529045] UIDs valid");
								// out.println("*" + " OK [UIDVALIDITY 3857529045] UIDs valid");

								// LOGGER.info("*" + " FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft)");
								// out.println("*" + " FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft)");


								LOGGER.info("< " + parts[0] + " OK [READ-WRITE] SELECT completed");
								out.println(parts[0] + " OK [READ-WRITE] SELECT completed");
							}
						}else{
							LOGGER.info("< " + parts[0] + " NO "+parts[1]+" "+requestedFolder+" failed" );
							out.println(parts[0] + " NO "+parts[1]+" failed");
						}
						
						
					}




					// ------------------- FETCH -------------------- 
					else if ( inputLine.matches("(?i)\\d+ UID fetch [^\"]+ [^\"]+") ) { 
						//Matches: UID fetch 1:* (FLAGS)
						String[] parts = inputLine.split(" ");
						
						if(parts[3].equals("1:*")){		//Pide todos los correos
							List<Map<String,String>> allMailsInfo = (new SQLiteJDBC()).getUserAllMailIDs(loggedUser);

							int count = 0;
							for (Map<String,String> mail : allMailsInfo) {
								String flags = "(";
								if(mail.get("recent").equals("1")) flags +="\\Recent";
								if(mail.get("seen").equals("1")) flags +="\\Seen";
								flags += "))"; //Cierra 2 paréntesis por el de (UID ...
								count++;
								LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags);
								out.println("* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags);
							}

							LOGGER.info("< " + parts[0] + " OK FETCH completed");
							out.println(parts[0] + " OK FETCH completed");


						}else if(parts[3].matches("\\d+:\\d+")){		//forma a:b , pide del correo a al b
							int firstMail = Integer.parseInt(parts[3].split(":")[0]);
							int lastMail = Integer.parseInt(parts[3].split(":")[0]);
							
							List<Map<String,String>> mailsInfo = (new SQLiteJDBC()).getUserMailsRange(loggedUser, firstMail, lastMail);

							LOGGER.info(">>>>>> mailsInfo.length: "+Integer.toString(mailsInfo.size()));


							int count = 0;
							for (Map<String,String> mail : mailsInfo) {
								String flags = "(";
								if(mail.get("recent").equals("1")) flags +="\\Recent";
								if(mail.get("seen").equals("1")) flags +="\\Seen";
								flags += ")"; //Cierra paréntesis de flags
								count++;
								
								LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)]) {"+mail.get("body").split("\n\n")[0].length()+"}"+mail.get("body").split("\n\n")[0]+")");
								out.println("* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)]) {"+mail.get("body").split("\n\n")[0].length()+"}"+mail.get("body").split("\n\n")[0]+")");
							}

							LOGGER.info("< " + parts[0] + " OK UID FETCH completed");
							out.println(parts[0] + " OK UID FETCH completed");


						}else{
							LOGGER.info("< " + parts[0] + " NO FETCH failed" );
							out.println(parts[0] + " NO FETCH failed");
						}
						
						
					}
					
					
					// ------------------- CAPABILITY -------------------- 
					else if ( inputLine.matches("(?i)\\d+ capability") ) {
						//Matches: NUM capability
						String[] parts = inputLine.split(" ");
						
						out.println(parts[0] + " OK [CAPABILITY IMAP4rev1 LITERAL+ SASL-IR LOGIN-REFERRALS ID ENABLE IDLE SORT SORT=DISPLAY THREAD=REFERENCES THREAD=REFS THREAD=ORDEREDSUBJECT MULTIAPPEND URL-PARTIAL CATENATE UNSELECT CHILDREN NAMESPACE UIDPLUS LIST-EXTENDED I18NLEVEL=1 CONDSTORE QRESYNC ESEARCH ESORT SEARCHRES WITHIN CONTEXT=SEARCH LIST-STATUS BINARY MOVE NOTIFY SPECIAL-USE QUOTA]" );
						LOGGER.info("< " + parts[0] + " OK [CAPABILITY IMAP4rev1 LITERAL+ SASL-IR LOGIN-REFERRALS ID ENABLE IDLE SORT SORT=DISPLAY THREAD=REFERENCES THREAD=REFS THREAD=ORDEREDSUBJECT MULTIAPPEND URL-PARTIAL CATENATE UNSELECT CHILDREN NAMESPACE UIDPLUS LIST-EXTENDED I18NLEVEL=1 CONDSTORE QRESYNC ESEARCH ESORT SEARCHRES WITHIN CONTEXT=SEARCH LIST-STATUS BINARY MOVE NOTIFY SPECIAL-USE QUOTA]" );
					} 
					
					// ------------------- NOOP -------------------- 
					else if ( inputLine.matches("(?i)\\d+ nooppp") ) {
						//Matches: NUM capability
						String[] parts = inputLine.split(" ");
						
						out.println(parts[0] + "NOOP" );
						LOGGER.info("< " + parts[0] + "NOOP" );
					}
					
					// ------------------- LOGOUT -------------------- 
					else if ( inputLine.matches("(?i)\\d+ logouttt") ) {
						//Matches: NUM capability
						String[] parts = inputLine.split(" ");
						
						authenticated = false;
						loggedUser = "";
						out.println(parts[0] + " OK logout completed" );
						LOGGER.info("< " + parts[0] + " OK logout completed" );
					} else {
						String[] parts = inputLine.split(" ");
						// LOGGER.info("< " + parts[0] + " NO "+parts[1]+" failed" );
						LOGGER.info("< " + parts[0] + " OK "+parts[1]+" completed" );
						out.println(parts[0] + " OK "+parts[1]+" completed");
						// out.println(parts[0] + " NO "+parts[1]+" failed");
					}
				}
			} catch (SocketException e) {
				System.out.println("-------- Se cerró la conexión con el cliente.");
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
}