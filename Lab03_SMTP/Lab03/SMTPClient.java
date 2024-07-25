import java.io.*;
import java.net.*;

public class SMTPClient {

	private String SERVER = "localhost";
	private String SERVERNAME = "test.ug";
	private int PORT = 25;
	Socket socket;

	BufferedReader in;
	PrintWriter out;
	BufferedReader stdIn;


	//Constructores
	public SMTPClient(String server,int port){
		this.SERVER = server;
		this.PORT = port;
		//servername Default test.ug
	}

	//COnstructor para tener nombre de server
	public SMTPClient(String server,int port, String serverName){
		this.SERVER = server;
		this.PORT = port;
		this.SERVERNAME = serverName;
	}

	public SMTPClient(){
		//No hacer nada, server y port default.
	}



	

	public static void main(String[] args) {
		//SMTPClient client = new SMTPClient();
		//client.start();
		//client.sendMail("julio@test.ug", "test@test.ug", "Este es el subject", "Este es el body.");
		//client.sendMail("julio@test.ug", "test@test.ug", "Este es el subject", "Este es el body \n ya le puse enter \n y ya. Saludos.");
	}

	public boolean start() {
		try{
			this.socket = new Socket(this.SERVER, this.PORT);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.stdIn = new BufferedReader(new InputStreamReader(System.in));

			return true;

			/*this.stdIn = new BufferedReader(new InputStreamReader(System.in))) {
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
			}*/
		} catch (IOException e) {
			System.out.println("Ocurrió un error inesperado. Saliendo.");
			return false;
		}
	}

	public void close(){
		try {
			this.socket.close();
			this.in.close();
			this.out.close();
			this.stdIn.close();
		} catch (Exception e) {
		
		}
	}
	

	public boolean sendMail(String fromParam, String toParamStr, String subjectParam, String bodyParam){
		try {
			System.out.println("INSIDE SENDMAIL FUNCTION");
			System.out.println("USING: ");
			System.out.println(this.SERVER);
			System.out.println(this.PORT);
			System.out.println(this.SERVERNAME);

			String fromServer;
			int i = 0;
			
			fromServer = in.readLine();
			System.out.println("-> fromServer = " + fromServer);
			
			out.println("HELO "+this.SERVERNAME);
			System.out.println("HELO "+this.SERVERNAME);
			
			fromServer = in.readLine();
			System.out.println("-> fromServer = " + fromServer);
			
			
			out.println("MAIL FROM: <"+fromParam+">");
			System.out.println("MAIL FROM: <"+fromParam+">");
			
			fromServer = in.readLine();
			System.out.println("-> fromServer = " + fromServer);
			
			String[] toParamArr = toParamStr.split(",");
			
			
			// Si hay múltiples TO
			for (String toStr : toParamArr) {
				out.println("RCPT TO: <"+toStr+">");
				System.out.println("RCPT TO: <"+toStr+">");
				fromServer = in.readLine();
				System.out.println("-> fromServer = " + fromServer);
			}
			
			
			out.println("DATA");
			System.out.println("DATA");
			
			
			fromServer = in.readLine();
			System.out.println("-> fromServer = " + fromServer);
			
			if(fromServer.startsWith("354")){
				out.println("Subject: "+subjectParam);
				System.out.println("Subject: "+subjectParam);
				
				out.println("From: "+fromParam);
				System.out.println("From: "+fromParam);
				
				out.println("To: "+toParamStr);
				System.out.println("To: "+toParamStr);
				
				out.println("");
				System.out.println("");
				
				out.println(bodyParam);
				System.out.println(bodyParam);
				
				out.println(".");
				System.out.println(".");

			}
			
			out.println("QUIT");
			System.out.println("QUIT");
			
			
			fromServer = in.readLine();
			System.out.println("-> fromServer = " + fromServer);

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		
		return true;
	}
}