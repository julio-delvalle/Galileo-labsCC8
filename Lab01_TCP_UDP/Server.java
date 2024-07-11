import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;


public class Server  {
    public static void main(String[] args) throws IOException{
        try {
            System.out.println("Hello, SERVER!");

            int port = 3000;
            int protocol = 1; /*1 = TCP, 2 = UDP */
            for (int i = 0; i < args.length; i++) {
                if("protocol".equals(args[i])){
                    if("TCP".equals(args[i+1])){
                        protocol = 1;
                    }else if("UDP".equals(args[i+1])){
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

            }


            if(protocol == 1){
                //USANDO TCP protocol = 1
                // serverSocket is for TCP
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Using TCP. Listening for clients on port "+port+"...");
                Socket clientSocket = serverSocket.accept();
                String clientSocketIP = clientSocket.getInetAddress().toString();
                int clientSocketPort = clientSocket.getPort();
                System.out.println("[IP: " + clientSocketIP + " ,Port: " + clientSocketPort +"]  " + "Client Connection Successful!");
                
        
                DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());
                
                String clientMessage = dataIn.readUTF();
                System.out.println(clientMessage);
                String serverMessage = "Hi this is coming from Server!";
                dataOut.writeUTF(serverMessage);
                
                dataIn.close();
                dataOut.close();
                serverSocket.close();
                clientSocket.close();
            }else if(protocol == 2){
                System.out.println("Using UDP. Listening for clients...");


                
            }


        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Falta un parámetro!");
        } catch (Exception e) {
            System.out.println("Exception! "+e);
        }
    }
}
