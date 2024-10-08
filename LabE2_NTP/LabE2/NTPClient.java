import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;



public class NTPClient {

    private static SimpleDateFormat loggerDateFormat = new SimpleDateFormat("yyMMddHHmm");


    private static final String[] NTP_SERVERS = {
        "time.google.com", // Google Time
        "time.apple.com", // Apple Time
        "time.windows.com", // Windows Time
        "time.cloudflare.com", // Cloudflare Time
        "cern.ch", // CERN Time
        "time.nist.gov", // NIST Time
        "time-a.nist.gov", // NIST Time A
        "time-b.nist.gov" // NIST Time B
    };

    


    public static void main(String[] args) throws IOException{
        try {
            System.out.println("Hello, CLIENT NTP!");


            // DEFINICION DE LOGGER:
            String logsFolder = "logs";
            Logger LOGGER = Logger.getAnonymousLogger();    
            LOGGER.setUseParentHandlers(false);
            Files.createDirectories(Paths.get(logsFolder));
            FileHandler fileHandler = new FileHandler(logsFolder + File.separator + "C" + "-" + loggerDateFormat.format((new Date()).getTime()) + ".log");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            fileHandler.setFormatter(new FormatterWebServer());
            consoleHandler.setFormatter(new FormatterWebServer());
            LOGGER.addHandler(fileHandler);
            LOGGER.addHandler(consoleHandler);
            
            
            int port = 123;
            int protocol = 2; /*1 = TCP, 2 = UDP */
            String server = NTP_SERVERS[0]; /*Default server Google time.*/
            
            //Scanner for reading keyboard input:
            Scanner keyboard = new Scanner(System.in);

            
            //For formating date and time:
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  


            if(protocol == 2){
                // Crear paquete UDP
                DatagramPacket sendPacket;
                byte[] sendData = new byte[48]; //48 bytes por default
                
                // UDP usa DatagramSocket
                DatagramSocket clientSocket = new DatagramSocket();
                // Set client timeout to be 2 seconds
                clientSocket.setSoTimeout(10000);

                
                String myIP = InetAddress.getLocalHost().getHostAddress();
                
                //Para recibir respuesta de server:
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket;


                System.out.println("Conectado a "+server+" en el puerto "+port+" usando UDP");


                //Valores a enviar:
                int leapIndicator = 0;
                int versionNumber = 3;
                int mode = 3;
                int stratum = 0;
                int pollInterval = 10;
                int precision = 0;

                boolean esperandoRespuesta = false;

                while (true) {

                    if(!esperandoRespuesta){
                        // =========== LEAP INDICATOR ===========
                        System.out.println("Ingrese el Leap Indicator (0, 1, 2 o 3), presione ENTER para default (0): ");
                        try{
                        leapIndicator = keyboard.nextInt();
                        if(leapIndicator < 0 || leapIndicator > 3){
                            System.out.println("Leap Indicator debe ser 0, 1, 2 o 3. Usando valor default (0).");
                            leapIndicator = 0;
                        }
                        }catch(Exception e){
                            System.out.println("Leap Indicator debe ser 0, 1, 2 o 3. Usando valor default (0).");
                            leapIndicator = 0;
                        }

                        // =========== MODE ===========
                        System.out.println("Ingrese el Modo (0 a 7), presione ENTER para default (3): ");
                        try{
                            mode = keyboard.nextInt();
                            if(mode < 0 || mode > 7){
                                System.out.println("Modo debe ser 0 a 7. Usando valor default (3).");
                                mode = 3;
                            }
                        }catch(Exception e){
                            System.out.println("Modo debe ser 0 a 7. Usando valor default (3).");
                            mode = 3;
                        }


                        // =========== STRATUM ===========
                        System.out.println("Ingrese el Stratum (0 a 15), presione ENTER para default (0): ");
                        try{
                            stratum = keyboard.nextInt();
                            if(stratum < 0 || stratum > 15){
                                System.out.println("Stratum debe ser 0 a 15. Usando valor default (0).");
                                stratum = 0;
                            }
                        }catch(Exception e){
                            System.out.println("Stratum debe ser 0 a 15. Usando valor default (0).");
                            stratum = 0;
                        }



                        // =========== POLL INTERVAL ===========
                        System.out.println("Ingrese el Poll Interval (0 a 255), presione ENTER para default (10): ");
                        try{
                            pollInterval = keyboard.nextInt();
                            if(pollInterval < 0 || pollInterval > 255){
                                System.out.println("Poll Interval debe ser 0 a 255. Usando valor default (10).");
                                pollInterval = 10;
                            }
                        }catch(Exception e){
                            System.out.println("Poll Interval debe ser 0 a 255. Usando valor default (10).");
                            pollInterval = 10;
                        }



                        // =========== PRECISION ===========
                        System.out.println("Ingrese la Precision (0 a 255), presione ENTER para default (6): ");
                        try{
                            precision = keyboard.nextInt();
                            if(precision < 0 || precision > 255){
                                System.out.println("Precision debe ser 0 a 255. Usando valor default (6).");
                                precision = 6;
                            }
                        }catch(Exception e){
                            System.out.println("Precision debe ser 0 a 255. Usando valor default (6).");
                            precision = 6;
                        }


                        // Convertir a bits los primeros 3 campos para combinarlos.
                        String leapBits = String.format("%2s", Integer.toBinaryString(leapIndicator)).replace(' ', '0');
                        String versionBits = String.format("%3s", Integer.toBinaryString(versionNumber)).replace(' ', '0');
                        String modeBits = String.format("%3s", Integer.toBinaryString(mode)).replace(' ', '0');
                        String combinedFirst3Bits = leapBits + versionBits + modeBits;

                        //Pasar a hexadecimal los primeros 3 campos combinados:
                        int combinedFirst3BitsValue = Integer.parseInt(combinedFirst3Bits, 2);


                        System.out.println("\n===== Se enviará paquete NTP con los siguientes valores: =====\n");
                        System.out.println("Leap Indicator: "+leapIndicator+" ("+leapBits+")");
                        System.out.println("Version Number: "+versionNumber+" ("+versionBits+")");
                        System.out.println("Mode: "+mode+" ("+modeBits+")");
                        System.out.println("Stratum: "+stratum);
                        System.out.println("Poll Interval: "+pollInterval);
                        System.out.println("Precision: "+precision);
                        System.out.println("\n============================================================\n");


                        esperandoRespuesta = false;

                        
                        //Armar paquete NTP:
                        sendData[0] = (byte)combinedFirst3BitsValue;
                        sendData[1] = (byte)stratum;
                        sendData[2] = (byte)pollInterval;
                        sendData[3] = (byte)precision;  
                        
                        System.out.println(byteArrayToHex(sendData));

                        //Todo lo demás va con 0 

                    }
                    
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(server), port);
                    clientSocket.send(sendPacket);
                    
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    String serverMessage = new String(receivePacket.getData(),0,receivePacket.getLength());
                    System.out.println("> "+server+" server ["+dtf.format(LocalDateTime.now())+"] UDP: "+serverMessage);
                    
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



    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
           sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
