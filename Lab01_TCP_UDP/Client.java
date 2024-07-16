import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class Client {
    public static void main(String[] args) throws IOException{
        try {
            System.out.println("Hello, CLIENT!");
            
            
            int port = 3000;
            int protocol = 1; /*1 = TCP, 2 = UDP */
            String server = "127.0.0.1"; /*Default server localhost.*/
            for (int i = 0; i < args.length; i++) {
                if("protocol".equals(args[i])){
                    if("TCP".equals(args[i+1]) || "tcp".equals(args[i+1])){
                        protocol = 1;
                    }else if("UDP".equals(args[i+1]) || "udp".equals(args[i+1])){
                        protocol = 2;
                    }else{
                        System.out.println("Select a valid Protocol! Using TCP as default.");
                        protocol = 1;
                    }
                }


                if("port".equals(args[i])){
                    try {
                        port = Integer.parseInt(args[i+1]);

                    } catch (NumberFormatException e) {
                        System.out.println("Port is not an integer! Using default port 3000");
                        port = 3000;
                    }
                    
                    if(port < 1 || port > 65535){
                        System.out.println("Port out of bounds! Using default port 3000");
                        port = 3000;
                    }
                }
                
                if("server".equals(args[i])){
                    try {
                        server = args[i+1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Server not defined! Using default server 127.0.0.1");
                        server = "127.0.0.1";
                    }
                }
            }



            
            //Scanner for reading keyboard input:
            Scanner keyboard = new Scanner(System.in);
            String userInput = "";

            
            //For formating date and time:
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  






            if(protocol == 1){
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(server, port), 2000);
                String socketIP = socket.getInetAddress().getHostAddress();

                System.out.println("Conexión exitosa!");   
        
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                // dataOut.writeUTF("Hello, This is coming from Client!");
                
                userInput = "";
                while (true) { 
                    System.out.print("Ingrese la operación a realizar: ");
                    userInput = keyboard.nextLine();
                    dataOut.writeUTF(userInput);
                    System.out.println("< "+socketIP+" client ["+dtf.format(LocalDateTime.now())+"] TCP: "+userInput);

                    String serverMessage = dataIn.readUTF();
                    System.out.println("> "+server+" server ["+dtf.format(LocalDateTime.now())+"] TCP: "+serverMessage);

                    if("EXIT".equals(userInput) || "exit".equals(userInput) || "Exit".equals(userInput)){
                        break;
                    }
                }
                System.out.println("...Bye!");
                
                dataIn.close();
                dataOut.close();
                socket.close();
                keyboard.close();

            }else if(protocol == 2){
                DatagramPacket sendPacket;
                byte[] sendData;

                // UDP usa DatagramSocket
                DatagramSocket clientSocket = new DatagramSocket();
                // Set client timeout to be 2 seconds
                clientSocket.setSoTimeout(2000);


                String myIP = InetAddress.getLocalHost().getHostAddress();

                //Para recibir respuesta de server:
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket;

                userInput = "";
                while (true) {
                    System.out.print("Ingrese la operación a realizar: ");
                    userInput = keyboard.nextLine();

                    sendData = userInput.getBytes();
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(server), port);
                    clientSocket.send(sendPacket);
                    System.out.println("< "+myIP+" client ["+dtf.format(LocalDateTime.now())+"] UDP: "+userInput);

                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    String serverMessage = new String(receivePacket.getData(),0,receivePacket.getLength());
                    System.out.println("> "+server+" server ["+dtf.format(LocalDateTime.now())+"] UDP: "+serverMessage);

                    if("EXIT".equals(userInput) || "exit".equals(userInput) || "Exit".equals(userInput)){
                        clientSocket.close();
                        System.exit(1);
                        break;
                    }
                    
                }

                
            }
        } catch (ConnectException e) {
            System.out.println("No se pudo establecer la conexión.");
        } catch (SocketTimeoutException e) {
            System.out.println("El servidor tardó mucho en responder. Saliendo.");
        } catch (Exception e) {
            System.out.println("Ocurrió un error inesperado ("+e.getClass().getSimpleName()+"). Saliendo. ");
        }
        
    }
}
