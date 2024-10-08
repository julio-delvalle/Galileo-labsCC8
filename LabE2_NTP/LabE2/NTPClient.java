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
        "time.asdfsdf.com", // Windows Time
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


            LOGGER.info("Hello, CLIENT NTP!\n\n");

            
            
            int port = 123;
            int protocol = 2; /*1 = TCP, 2 = UDP */
            int serverIndex = 0;
            
            //Scanner for reading keyboard input:
            Scanner keyboard = new Scanner(System.in);

            
            //For formating date and time:
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");  


            if(protocol == 2){
                // Crear paquete UDP
                DatagramPacket sendPacket;
                byte[] sendData = new byte[48]; //48 bytes por default
                
                // UDP usa DatagramSocket
                DatagramSocket clientSocket = new DatagramSocket();
                // Set client timeout to be 2 seconds
                clientSocket.setSoTimeout(5000);

                
                String myIP = InetAddress.getLocalHost().getHostAddress();
                
                //Para recibir respuesta de server:
                byte[] receiveData = new byte[48];
                DatagramPacket receivePacket;


                //Valores a enviar:
                int leapIndicator = 0;
                int versionNumber = 3;
                int mode = 3;
                int stratum = 0;
                int pollInterval = 10;
                int precision = -10;

                boolean esperandoRespuesta = false;

                int paquetesEnviados = 0;

                while (true) {
                    String server = NTP_SERVERS[serverIndex]; /*Default server Google time.*/

                    if(paquetesEnviados > 0 && !esperandoRespuesta){
                        System.out.print("\n\n¿Desea enviar otro paquete? (s/n), presione ENTER para default (s): ");
                        try{
                            String userInput = keyboard.nextLine();
                            if(!userInput.isEmpty()){
                                if(userInput.equals("s")){
                                }else{
                                    break;
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Respuesta debe ser s o n. Usando valor default (s).");
                        }
                    }


                    if(!esperandoRespuesta){
                        // =========== LEAP INDICATOR ===========
                        System.out.print("Ingrese el Leap Indicator\n0: ningún ajuste\n1: agregar 1s a fin de mes\n2: restar 1s a fin de mes\n3: estado de ajuste desconocido\n o presione ENTER para default (0): ");
                        try{
                            String userInput = keyboard.nextLine();
                            if(!userInput.isEmpty()){
                                leapIndicator = Integer.parseInt(userInput);
                                if(leapIndicator < 0 || leapIndicator > 3){
                                    System.out.println("Leap Indicator debe ser 0, 1, 2 o 3. Usando valor default (0).");
                                    leapIndicator = 0;
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Leap Indicator debe ser 0, 1, 2 o 3. Usando valor default (0).");
                            leapIndicator = 0;
                        }

                        // =========== MODE ===========
                        System.out.print("\nIngrese el Modo\n0: reservado\n1: Servidor-Servidor simétrico\n2: Servidor modo pasivo simétrico\n3: Cliente\n4: Servidor respondiendo a cliente\n5: Broadcast\n6: Mensaje de control\n7: reservado\n o presione ENTER para default (3): ");
                        try{
                            String userInput = keyboard.nextLine();
                            if(!userInput.isEmpty()){
                                mode = Integer.parseInt(userInput);
                                if(mode < 0 || mode > 7){
                                    System.out.println("Modo debe ser 0 a 7. Usando valor default (3).");
                                    mode = 3;
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Modo debe ser 0 a 7. Usando valor default (3).");
                            mode = 3;
                        }


                        // =========== STRATUM ===========
                        System.out.print("\nIngrese el Stratum\n0: Desconocido o no aplicable\n1: Primario\n2-15: Secundario\n16-255: No utilizado\n o presione ENTER para default (0): ");
                        try{
                            String userInput = keyboard.nextLine();
                            if(!userInput.isEmpty()){
                                stratum = Integer.parseInt(userInput);
                                if(stratum < 0 || stratum > 15){
                                    System.out.println("Stratum debe ser 0 a 15. Usando valor default (0).");
                                    stratum = 0;
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Stratum debe ser 0 a 15. Usando valor default (0).");
                            stratum = 0;
                        }



                        // =========== POLL INTERVAL ===========
                        System.out.print("\nIngrese el Poll Interval\n0: Ninguno\n1-255: Intervalo de tiempo en segundos\n o presione ENTER para default (10): ");
                        try{
                            String userInput = keyboard.nextLine();
                            if(!userInput.isEmpty()){
                                pollInterval = Integer.parseInt(userInput);
                                if(pollInterval < 0 || pollInterval > 255){
                                    System.out.println("Poll Interval debe ser 0 a 255. Usando valor default (10).");
                                    pollInterval = 10;
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Poll Interval debe ser 0 a 255. Usando valor default (10).");
                            pollInterval = 10;
                        }



                        // =========== PRECISION ===========
                        System.out.print("\nIngrese la Precisión\n0: Ninguno\n1-255: Precisión del reloj en segundos\n o presione ENTER para default (-10): ");
                        try{
                            String userInput = keyboard.nextLine();
                            if(!userInput.isEmpty()){
                                precision = Integer.parseInt(userInput);
                                if(precision < -127 || precision > 127){
                                    System.out.println("Precision debe ser -127 a 127. Usando valor default (-10).");
                                    precision = -10;
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Precision debe ser -127 a 127. Usando valor default (-10).");
                            precision = -10;
                        }


                    }

                    // Convertir a bits los primeros 3 campos para combinarlos.
                    String leapBits = String.format("%2s", Integer.toBinaryString(leapIndicator)).replace(' ', '0');
                    String versionBits = String.format("%3s", Integer.toBinaryString(versionNumber)).replace(' ', '0');
                    String modeBits = String.format("%3s", Integer.toBinaryString(mode)).replace(' ', '0');
                    String combinedFirst3Bits = leapBits + versionBits + modeBits;

                    //Pasar a hexadecimal los primeros 3 campos combinados:
                    int combinedFirst3BitsValue = Integer.parseInt(combinedFirst3Bits, 2);                    

                    
                    //Armar paquete NTP:
                    sendData[0] = (byte)combinedFirst3BitsValue;
                    sendData[1] = (byte)stratum;
                    sendData[2] = (byte)pollInterval;
                    sendData[3] = (byte)precision;  
                    sendData[11] = (byte)1;  
                    //Todo lo demás va con 0 !!!!!!!

                    
                    System.out.println();
                    LOGGER.info("===== Se enviará paquete NTP con los siguientes valores: =====");
                    LOGGER.info("Server: "+server);
                    LOGGER.info("Leap Indicator: "+leapIndicator+" ("+leapBits+")");
                    LOGGER.info("Version Number: "+versionNumber+" ("+versionBits+")");
                    LOGGER.info("Mode: "+mode+" ("+modeBits+")");
                    LOGGER.info("Stratum: "+stratum);
                    LOGGER.info("Poll Interval: "+pollInterval);
                    LOGGER.info("Precision: "+precision);
                    LOGGER.info("============================================================\n");


                    LOGGER.info("Paquete NTP enviado: "+byteArrayToHex(sendData));



                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(server), port);
                    clientSocket.send(sendPacket);
                    paquetesEnviados++;
                    esperandoRespuesta = true;

                    try{
                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        clientSocket.receive(receivePacket);
                        String serverMessage = byteArrayToHex(receivePacket.getData());
                        LOGGER.info(server+" > "+serverMessage);

                        //Analizar aquí la respuesta (proceso inverso de construir el paquete)
                        int responseFirst3Values = (int)receivePacket.getData()[0] & 0xFF;
                        String responseFirst3ValuesBits = String.format("%8s", Integer.toBinaryString(responseFirst3Values)).replace(' ', '0');
                        String responseLeapBits = String.format("%2s", responseFirst3ValuesBits.substring(0, 2)).replace(' ', '0');
                        int responseLeapIndicator = Integer.parseInt(responseLeapBits, 2);
                        String responseVersionBits = String.format("%3s", responseFirst3ValuesBits.substring(2, 5)).replace(' ', '0');
                        int responseVersionNumber = Integer.parseInt(responseVersionBits, 2);
                        String responseModeBits = String.format("%3s", responseFirst3ValuesBits.substring(5)).replace(' ', '0');
                        int responseMode = Integer.parseInt(responseModeBits, 2);

                        int responseStratum = receivePacket.getData()[1] & 0xFF;
                        int responsePollInterval = receivePacket.getData()[2] & 0xFF;
                        int responsePrecision = (int)receivePacket.getData()[3];

                        System.out.println();
                        LOGGER.info("===== RESPUESTA RECIBIDA: =====");
                        LOGGER.info("Server: "+server);
                        LOGGER.info("Leap Indicator: "+responseLeapIndicator+" ("+responseLeapBits+")");
                        LOGGER.info("Version Number: "+responseVersionNumber+" ("+responseVersionBits+")");
                        LOGGER.info("Mode: "+responseMode+" ("+responseModeBits+")");
                        LOGGER.info("Stratum: "+responseStratum);
                        LOGGER.info("Poll Interval: "+responsePollInterval);
                        LOGGER.info("Precision: "+responsePrecision);

                        int responseRootDelay = ((receivePacket.getData()[4] & 0xFF) << 24) |
                                                ((receivePacket.getData()[5] & 0xFF) << 16) |
                                                ((receivePacket.getData()[6] & 0xFF) << 8) |
                                                (receivePacket.getData()[7] & 0xFF);
                        LOGGER.info("Root Delay: " + responseRootDelay);

                        int responseRootDispersion = ((receivePacket.getData()[8] & 0xFF) << 24) |
                                                    ((receivePacket.getData()[9] & 0xFF) << 16) |
                                                    ((receivePacket.getData()[10] & 0xFF) << 8) |
                                                    (receivePacket.getData()[11] & 0xFF);
                        LOGGER.info("Root Dispersion: " + responseRootDispersion);


                        StringBuilder responseReferenceIDBuilder = new StringBuilder();
                        for (int i = 12; i <= 15; i++) {
                            responseReferenceIDBuilder.append(String.format("%02x", receivePacket.getData()[i] & 0xFF));
                        }
                        String responseReferenceID = responseReferenceIDBuilder.toString();
                        LOGGER.info("Reference ID: " + responseReferenceID);



                        StringBuilder responseReferenceTimestampBuilder = new StringBuilder();
                        for (int i = 16; i <= 23; i++) {
                            responseReferenceTimestampBuilder.append(String.format("%02x", receivePacket.getData()[i] & 0xFF));
                        }
                        String responseReferenceTimestamp = responseReferenceTimestampBuilder.toString();
                        LOGGER.info("Reference Timestamp: " + responseReferenceTimestamp);




                        StringBuilder responseOriginTimestampBuilder = new StringBuilder();
                        for (int i = 24; i <= 31; i++) {
                            responseOriginTimestampBuilder.append(String.format("%02x", receivePacket.getData()[i] & 0xFF));
                        }
                        String responseOriginTimestamp = responseOriginTimestampBuilder.toString();
                        LOGGER.info("Origin Timestamp: " + responseOriginTimestamp);




                        StringBuilder responseReceiveTimestampBuilder = new StringBuilder();
                        for (int i = 32; i <= 39; i++) {
                            responseReceiveTimestampBuilder.append(String.format("%02x", receivePacket.getData()[i] & 0xFF));
                        }
                        String responseReceiveTimestamp = responseReceiveTimestampBuilder.toString();
                        LOGGER.info("Receive Timestamp: " + responseReceiveTimestamp);




                        StringBuilder responseTransmitTimestampBuilder = new StringBuilder();
                        for (int i = 40; i <= 47; i++) {
                            responseTransmitTimestampBuilder.append(String.format("%02x", receivePacket.getData()[i] & 0xFF));
                        }
                        String responseTransmitTimestamp = responseTransmitTimestampBuilder.toString();
                        long unsignedValue = Long.parseLong(responseTransmitTimestamp.substring(0, 8), 16);
                        LOGGER.info("Transmit Timestamp: " + responseTransmitTimestamp + " (" + unsignedValue + ")");

                        LOGGER.info("============================================================\n");

                        
                        esperandoRespuesta = false;
                    }catch(SocketTimeoutException e){
                        LOGGER.log(Level.WARNING, "El servidor tardó mucho en responder. Cambiando de servidor.");
                        serverIndex++;
                    }catch(Exception e){
                        LOGGER.log(Level.WARNING, "Error al recibir el paquete: "+e.getMessage());
                        esperandoRespuesta = false;
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



    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
           sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
