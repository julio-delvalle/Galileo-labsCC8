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

				while ((inputLine = in.readLine()) != null) {
					
					//Linea recibida:
					System.out.println("> " + inputLine );


					//Matches: NUM login "asdfasdf" "admin"
					if ( inputLine.matches("(?i)\\d+ login \"[^\"]+\" \"[^\"]+\"") ) {
						String[] parts = inputLine.split(" ");
						System.out.println("< " + parts[0] + " OK LOGIN completed" );
						

						out.println(parts[0] + " OK LOGIN completed" );
					} else {
						String[] parts = inputLine.split(" ");
						System.out.println(parts[0] + " NO "+parts[1]+" failed" );
						out.println(parts[0] + " NO "+parts[1]+" failed");
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