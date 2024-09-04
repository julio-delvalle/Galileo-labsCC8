import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Server  {
    public static void main(String[] args) throws IOException{
        try {
                DatagramSocket serverSocket = new DatagramSocket(port);
                System.out.println("Using UDP. Listening for clients on port "+port+"...");

                //String serverSocketIP = serverSocket.getLocalAddress().getHostAddress();
                String serverSocketIP = InetAddress.getLocalHost().getHostAddress();
                
                // Assume messages are not over 1024 bytes
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket;
                DatagramPacket sendPacket;
                byte[] sendData;
                
                while (true) {
                    // Server waiting for clients message
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    // Get the clients IP
                    //String clientIPAddress = receivePacket.getAddress().getHostAddress();
                    int clientPort = receivePacket.getPort();


                    String clientMessage = new String(receivePacket.getData(),0,receivePacket.getLength());
                    System.out.println("> "+receivePacket.getAddress()+" client ["+dtf.format(LocalDateTime.now())+"] UDP: "+clientMessage);

                    /*  ====== UDP NO SE CIERRA AL RECIBIR EXIT ========*/
                    
                    if("EXIT".equals(clientMessage) || "exit".equals(clientMessage) || "Exit".equals(clientMessage)){
                        
                        sendData = clientMessage.getBytes();
                        sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), clientPort);
                        serverSocket.send(sendPacket);

                        System.out.println("< "+receivePacket.getAddress()+" server ["+dtf.format(LocalDateTime.now())+"] UDP: "+clientMessage);
                        break;
                    }


                    char operator = ' ';
                    int operatorIndex = -1;

                    try {
                        for (int i = 0; i < clientMessage.length(); i++) {
                            char ch = clientMessage.charAt(i);
                            if(ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%'){
                                operator = ch;
                                operatorIndex = i;
                                break;
                            }
                        }

                        float num1 = 0.0F;
                        float num2 = 0.0F;

                        if(operator == ' '){
                            num1 = Float.parseFloat(clientMessage);
                        }else{
                            num1 = Float.parseFloat(clientMessage.substring(0,operatorIndex));
                            num2 = Float.parseFloat(clientMessage.substring(operatorIndex+1)); //número 2 comienza después de operatorIndex hasta el final
                        }

                        float result = 0F;
                        switch (operator) {
                            case '+' -> result = num1 + num2;
                            case '-' -> result = num1 - num2;
                            case '*' -> result = num1 * num2;
                            case '/' -> result = num1 / num2;
                            case '%' -> result = num1 % num2;
                            case ' ' -> result = num1;
                        
                            default -> {
                            }
                        }


                        sendData = Float.toString(result).getBytes();
                        sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), clientPort);
                        serverSocket.send(sendPacket);

                        System.out.println("< "+serverSocketIP+" server ["+dtf.format(LocalDateTime.now())+"] UDP: "+Float.toString(result));

                    } catch (Exception e) {
                        sendData = "Operación no válida!".getBytes();
                        sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), clientPort);
                        serverSocket.send(sendPacket);
                        System.out.println("< "+serverSocketIP+" server ["+dtf.format(LocalDateTime.now())+"] UDP: "+"Operación no válida!");
                    }


                }

                //serverSocket.close();


        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Falta un parámetro!");
        } catch (SocketException e) {
            System.out.println("Se perdió la conexión! ");
        } catch (Exception e) {
            //System.out.println("Ocurrió un error inesperado ("+e.getClass().getSimpleName()+"). Saliendo. ");
            System.out.println("Ocurrió un error inesperado ("+e+"). Saliendo. ");
        }
    }
}
