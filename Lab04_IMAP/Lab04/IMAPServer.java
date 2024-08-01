import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

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

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// Enviar mensaje de bienvenida
				out.println("* OK [CAPABILITY IMAP4rev1] IMAP Server Ready");
				System.out.println(" > * OK [CAPABILITY IMAP4rev1] IMAP Server Ready");

				String inputLine;
				boolean authenticated = false;
				String loggedUser = "";

				while ((inputLine = in.readLine()) != null) {
					
					//Linea recibida:
					System.out.println("> " + inputLine );


					// ------------------- LOGIN -------------------- 
					if ( inputLine.matches("(?i)\\d+ loginnn \"[^\"]+\" \"[^\"]+\"") ) {
						//Matches: NUM login "asdfasdf" "admin"
						String[] parts = inputLine.split(" ");

						
						if(parts[3].replace("\"","").equals("admin")){
							System.out.println("< " + parts[0] + " OK LOGIN completed" );
							authenticated = true;
							loggedUser = parts[2].replace("\"","");
							out.println(parts[0] + " OK LOGIN completed" );
						}else{
							System.out.println("< " + parts[0] + " NO LOGIN incorrect password" );
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
							if(inboxInfo.containsKey("exists")){
								// System.out.println("< " + parts[0] + " "+inboxInfo.get("exists")+" EXISTS" );
								// out.println( parts[0] + " "+inboxInfo.get("exists")+" EXISTS" );
								// System.out.println("*" + " 2 RECENT");
								// out.println("*" + " 2 RECENT" );
								// out.println( parts[0] + " 1 RECENT" );
								// System.out.println("*" + " OK [UNSEEN 8] First unseen.");
								// out.println("*" + " OK [UNSEEN 8] First unseen.");
								// out.println(parts[0] + " OK [UNSEEN 2] First unseen.");
								// System.out.println("< " + parts[0] + " OK [UIDVALIDITY 3857529045] UIDs valid");
								// out.println("*" + " OK [UIDVALIDITY 3857529045] UIDs valid");
								// System.out.println("*" + " FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft)");
								// out.println("*" + " FLAGS (\\Answered \\Flagged \\Deleted \\Seen \\Draft)");
								System.out.println("< " + parts[0] + " OK select completed");
								out.println(parts[0] + " OK select completed");
							}
						}else{
							System.out.println("< " + parts[0] + " NO "+parts[1]+" "+requestedFolder+" failed" );
							out.println(parts[0] + " NO "+parts[1]+" failed");
						}
						
						
					}
					
					
					// ------------------- CAPABILITY -------------------- 
					else if ( inputLine.matches("(?i)\\d+ capability") ) {
						//Matches: NUM capability
						String[] parts = inputLine.split(" ");
						
						out.println(parts[0] + " OK [CAPABILITY IMAP4rev1 LITERAL+ SASL-IR LOGIN-REFERRALS ID ENABLE IDLE SORT SORT=DISPLAY THREAD=REFERENCES THREAD=REFS THREAD=ORDEREDSUBJECT MULTIAPPEND URL-PARTIAL CATENATE UNSELECT CHILDREN NAMESPACE UIDPLUS LIST-EXTENDED I18NLEVEL=1 CONDSTORE QRESYNC ESEARCH ESORT SEARCHRES WITHIN CONTEXT=SEARCH LIST-STATUS BINARY MOVE NOTIFY SPECIAL-USE QUOTA]" );
						// out.println(parts[0] + " OK [CAPABILITY IMAP4rev1]" );
						// System.out.println("< " + parts[0] + " OK [CAPABILITY IMAP4rev1]" );
						System.out.println("< " + parts[0] + " OK [CAPABILITY IMAP4rev1 LITERAL+ SASL-IR LOGIN-REFERRALS ID ENABLE IDLE SORT SORT=DISPLAY THREAD=REFERENCES THREAD=REFS THREAD=ORDEREDSUBJECT MULTIAPPEND URL-PARTIAL CATENATE UNSELECT CHILDREN NAMESPACE UIDPLUS LIST-EXTENDED I18NLEVEL=1 CONDSTORE QRESYNC ESEARCH ESORT SEARCHRES WITHIN CONTEXT=SEARCH LIST-STATUS BINARY MOVE NOTIFY SPECIAL-USE QUOTA]" );
					} 
					
					// ------------------- NOOP -------------------- 
					else if ( inputLine.matches("(?i)\\d+ nooppp") ) {
						//Matches: NUM capability
						String[] parts = inputLine.split(" ");
						
						out.println(parts[0] + "NOOP" );
						System.out.println("< " + parts[0] + "NOOP" );
					}
					
					// ------------------- LOGOUT -------------------- 
					else if ( inputLine.matches("(?i)\\d+ logouttt") ) {
						//Matches: NUM capability
						String[] parts = inputLine.split(" ");
						
						authenticated = false;
						loggedUser = "";
						out.println(parts[0] + " OK logout completed" );
						System.out.println("< " + parts[0] + " OK logout completed" );
					} else {
						String[] parts = inputLine.split(" ");
						// System.out.println("< " + parts[0] + " NO "+parts[1]+" failed" );
						System.out.println("< " + parts[0] + " OK "+parts[1]+" completed" );
						out.println(parts[0] + " OK "+parts[1]+" completed");
						// out.println(parts[0] + " NO "+parts[1]+" failed");
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
}