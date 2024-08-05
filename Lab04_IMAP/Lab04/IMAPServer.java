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
				String lastSelect = "";

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
						lastSelect = requestedFolder;

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
						}else if(requestedFolder.equals("Sent")){
							HashMap sentInfo = (new SQLiteJDBC()).getSentInfo(loggedUser);
							if(sentInfo.containsKey("exists") && sentInfo.containsKey("recent")){
								LOGGER.info("< " + "* "+sentInfo.get("exists")+" EXISTS" );
								out.println("* "+sentInfo.get("exists")+" EXISTS" );
								
								LOGGER.info("< " + "* "+sentInfo.get("recent")+" RECENT" );
								out.println("* "+sentInfo.get("recent")+" RECENT" );

								// out.println("*" + " OK [UNSEEN "+sentInfo.get("firstunseen")+"] First unseen.");
								// LOGGER.info("< *" + " OK [UNSEEN "+sentInfo.get("firstunseen")+"] First unseen.");
								// out.println(parts[0] + " OK [UNSEEN 2] First unseen.");

								LOGGER.info("< " + parts[0] + " OK [READ-WRITE] SELECT completed");
								out.println(parts[0] + " OK [READ-WRITE] SELECT completed");
							}
						}else{
							LOGGER.info("< " + parts[0] + " NO "+parts[1]+" "+requestedFolder+" failed" );
							out.println(parts[0] + " NO "+parts[1]+" failed");
						}
						
						
					}





					// ------------------- LIST -------------------- 
					else if ( inputLine.matches("(?i)\\d+ list \"[^\"]*\" \"[^\"]*\"") ) {
						//Matches: NUM list "" "Sent"
						String[] parts = inputLine.split(" ");
						
						String requestedFolder1 = parts[2].replace("\"","");
						String requestedFolder2 = parts[3].replace("\"","");

						LOGGER.info("------------ RECIBÍ: rf1:"+requestedFolder1+"  rf2:"+requestedFolder2);

						if(requestedFolder1.equals("") && requestedFolder2.equals("Sent")){
								LOGGER.info("< " + "* LIST (\\HasNoChildren) \"\" \"Sent\"");
								out.println("* LIST (\\HasNoChildren) \"\" \"Sent\"");

								LOGGER.info("< " + parts[0] + " OK LIST completed");
								out.println(parts[0] + " OK LIST completed");
						}else{
							LOGGER.info("< " + parts[0] + " OK LIST completed" );
							out.println(parts[0] + " OK LIST completed" );
						}
						
					}




					// ------------------- FETCH -------------------- 
					else if ( inputLine.matches("(?i)\\d+ UID fetch [^\"]+ [^\"]+") ) { 
						//Matches: UID fetch 1:* (FLAGS)
						String[] parts = inputLine.split(" ");



						//SI TRAE EL FLAG BODY[] mandar TODO. SI trae más cosas, o BODY[ ... mandar solo header y todas las etiquetas.
						boolean sendBody = false;
						if(parts.length == 7 && parts[6].equals("BODY[])")){
							sendBody = true;
						}
						
						if(parts[3].matches("\\d+:\\*")){		//Pide todos los correos a partir del primer número
							int firstMail = Integer.parseInt(parts[3].split(":")[0]);

							List<Map<String,String>> allMailsInfo;
							if(lastSelect.equals("Sent")){
								allMailsInfo = (new SQLiteJDBC()).getUserSentMailIDs(loggedUser, firstMail);
							}else{
								allMailsInfo = (new SQLiteJDBC()).getUserAllMailIDs(loggedUser, firstMail);
							}

							int count = 0;
							for (Map<String,String> mail : allMailsInfo) {
								String flags = "(";
								if(mail.get("recent").equals("1")) flags +=" \\Recent";
								if(mail.get("seen").equals("1")) flags +=" \\Seen";
								flags += "))"; //Cierra 2 paréntesis por el de (UID ...
								count++;
								LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags);
								out.println("* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags);
							}

							LOGGER.info("< " + parts[0] + " OK FETCH completed");
							out.println(parts[0] + " OK FETCH completed");


						}else if(parts[3].matches("\\d+:\\d+") || parts[3].matches("\\d+:\\d+[,\\d+]*") || parts[3].matches("\\d+:\\d+[,\\d+:\\d+]*") || parts[3].matches("\\d+,[\\d+]*[,\\d+:\\d+]*")){		//forma a:b , pide del correo a al b. Puede venir como a:b, c . O venir como a:b, c:d
							int firstMail = Integer.parseInt(parts[3].split(":")[0].split(",")[0]);
							int lastMail = Integer.parseInt(parts[3].split(":")[parts[3].split(":").length-1].split(",")[parts[3].split(":")[parts[3].split(":").length-1].split(",").length-1]);
							
							List<Map<String,String>> mailsInfo;
							if(lastSelect.equals("Sent")){
								mailsInfo = (new SQLiteJDBC()).getUserSentMailsRange(loggedUser, firstMail, lastMail);
							}else{
								mailsInfo = (new SQLiteJDBC()).getUserMailsRange(loggedUser, firstMail, lastMail);
							}


							int count = 0;
							for (Map<String,String> mail : mailsInfo) {
								String flags = "(";
								if(mail.get("recent").equals("1")) flags +=" \\Recent";
								if(mail.get("seen").equals("1")) flags +=" \\Seen";
								flags += ")"; //Cierra paréntesis de flags
								count++;

								String bodyToSend = sendBody ? mail.get("body") : mail.get("body").split("\n\n")[0] ; //mail.get("body").split("\n\n")[0] es el HEADER SOLO.
								
								if(sendBody){
									//Mandar TODO, sin flags
									LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" RFC822.SIZE "+bodyToSend.length()+" BODY[] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");
									out.println("* "+count+" FETCH (UID "+mail.get("id")+" RFC822.SIZE "+bodyToSend.length()+" BODY[] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");
								}else{
									//Mandar solo header, con todos los flags
									LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");
									out.println("* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");
								}

							}

							LOGGER.info("< " + parts[0] + " OK UID FETCH completed");
							out.println(parts[0] + " OK UID FETCH completed");


						}else if(parts[3].matches("\\d+")){		//forma a -- pide un solo correo
							int mailNumber = Integer.parseInt(parts[3]);
							
							List<Map<String,String>> mailsInfo = (new SQLiteJDBC()).getUserSingleMail(loggedUser, mailNumber);


							int count = 0;
							for (Map<String,String> mail : mailsInfo) {
								String flags = "(";
								if(mail.get("recent").equals("1")) flags +=" \\Recent";
								if(mail.get("seen").equals("1")) flags +=" \\Seen";
								flags += ")"; //Cierra paréntesis de flags
								count++;

								String bodyToSend = sendBody ? mail.get("body") : mail.get("body").split("\n\n")[0] ; //mail.get("body").split("\n\n")[0] es el HEADER SOLO.
								
								if(sendBody){
									//Mandar TODO, sin flags
									LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" RFC822.SIZE "+bodyToSend.length()+" BODY[] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");
									out.println("* "+count+" FETCH (UID "+mail.get("id")+" RFC822.SIZE "+bodyToSend.length()+" BODY[] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");

									//Si entró aquí es porque se entró a ver el mail. Marcar como visto en base de datos:
									(new SQLiteJDBC()).setMailAsSeen(loggedUser, mailNumber);
								}else{
									//Mandar solo header, con todos los flags
									LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");
									out.println("* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)] {"+bodyToSend.length()+"}"+bodyToSend+"\n)");
								}
								// LOGGER.info("< " + "* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)] {"+bodyToSend.length()+"}"+bodyToSend+")");
								// out.println("* "+count+" FETCH (UID "+mail.get("id")+" FLAGS "+flags+" BODY[HEADER.FIELDS (From To Cc Bcc Subject Date Message-ID Priority X-Priority References Newsgroups In-Reply-To Content-Type Reply-To)] {"+bodyToSend.length()+"}"+bodyToSend+")");
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
						if(parts.length>1){
							LOGGER.info("< " + parts[0] + " OK "+parts[1]+" completed" );
							out.println(parts[0] + " OK "+parts[1]+" completed");
						}else{
							LOGGER.info("< " + parts[0] + " OK completed" );
							out.println(parts[0] + " OK completed");
						}
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