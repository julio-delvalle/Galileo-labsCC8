import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;


public class ThreadServer implements Runnable {
    private Integer nThreadServer;

    byte[] input = new byte[65535];
    private Integer delay;
    private DatagramSocket socket;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
    // static Logger LOGGER = Logger.getLogger("Server");
    // DEFINICION DE LOGGER:
    public String logsFolder = "logs";
    public Logger LOGGER = Logger.getAnonymousLogger();
    

    private static final int A = 1;
    private static final int CNAME = 5;
    private static final int TXT = 16;
    private static final int AAAA = 28;

    static HashMap<String, String> listIPv4 = new HashMap<>();
    static HashMap<String, String> listIPv6 = new HashMap<>();
    static HashMap<String, String> listTXT = new HashMap<>();
    static HashMap<String, String> listCNAME = new HashMap<>();

    private static final String[] DNS_SERVERS = {
            "8.8.8.8", // Google
            "8.8.4.4", // Google
            "208.67.222.222", // OpenDNS Home
            "4.2.2.1", // Level3
            "8.26.56.26", // Comodo Secure
            "84.200.69.80", // DNS.WATCH
            "9.9.9.9", // Quad9
            "1.1.1.1"  // Cloudflare
    };

    private static final String[] ROOT_SERVERS = {
            "198.41.0.4",       // A Root
            "199.9.14.201",     // B Root
            "192.33.4.12",      // C Root
            "199.7.91.13",      // D Root
            "192.203.230.10",   // E Root
            "192.5.5.241",      // F Root
            "192.112.36.4",     // G Root
            "198.97.190.53",    // H Root
            "192.36.148.17",    // I Root
            "192.58.128.30",    // J Root
            "193.0.14.129",     // K Root
            "199.7.83.42",      // L Root
            "202.12.27.33"      // M Root
    };

    public ThreadServer(Integer id, DatagramSocket socket, Integer delay) throws IOException {
        this.nThreadServer = id;
        this.socket = socket;
        this.delay = delay;

        listIPv4.put("www.youtube.com", "142.250.217.238");
        listIPv4.put("www.google.com", "8.8.8.8");

        LOGGER.setUseParentHandlers(false);
        Files.createDirectories(Paths.get(logsFolder));
        FileHandler fileHandler = new FileHandler(logsFolder + File.separator + "T"+ nThreadServer + "-" + dateFormat.format((new Date()).getTime()) + ".log");
        ConsoleHandler consoleHandler = new ConsoleHandler();
        fileHandler.setFormatter(new FormatterWebServer());
        consoleHandler.setFormatter(new FormatterWebServer());
        LOGGER.addHandler(fileHandler);
        LOGGER.addHandler(consoleHandler);
    }


    class RequestInfo {
        private InetAddress address;
        private int port;

        public RequestInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }
    }

    @Override
    public void run(){
        
        
        
            byte[] input = new byte[65535];
        DatagramPacket receivePacket = null;
        InetAddress clientAddress = null;
        int clientPort = 0;
        HashMap<String, RequestInfo> requesters = new HashMap<>();
        int dnsCounter = 0;
        int querysWithoutResponse = 0;

        
        while (true) {
            try {
                LOGGER.info("("+nThreadServer+") > Thread waiting for new client....");
                receivePacket = new DatagramPacket(input, input.length);
                socket.receive(receivePacket);

                if(querysWithoutResponse > 50){
                    System.out.println("SE LLENO QUERYS WITHOUT RESPONSE, SE VA A CAMBIAR DE DNS.");
                    dnsCounter++;
                    querysWithoutResponse = 0;
                }
                if(dnsCounter > 7){dnsCounter=0;}
                System.out.println("QuerysWithoutResponse: "+querysWithoutResponse);
                
                // int dnsCounter = new Random().nextInt(7);
                if(receivePacket.getAddress().toString().equals("/127.0.0.1")){
                    clientAddress = receivePacket.getAddress();
                    clientPort = receivePacket.getPort();

                    // ==== SI ES LOCAL, ES REQUEST. ENVIAR A DNS ====

                    DNSRequest receivedRequest = formatAndGetDNSRequest(receivePacket.getData(), receivePacket.getLength());
                    receivedRequest.setRecursionDesired();
                    receivedRequest.printDNSRequest();

                    requesters.put(receivedRequest.getTransactionID(), new RequestInfo(receivePacket.getAddress(), receivePacket.getPort()));
                    querysWithoutResponse++;

                    LOGGER.info("\n\n\n");


                    //Resend the query to the first DNS server
                    InetAddress dnsServer = InetAddress.getByName(DNS_SERVERS[dnsCounter]);
                    // if(dnsCounter == 8){break;}
                    // InetAddress dnsServer = InetAddress.getByName(ROOT_SERVERS[0]);
                    DatagramPacket dnsPacket = new DatagramPacket(receivedRequest.getData(), receivedRequest.getLength(), dnsServer,53);
                    LOGGER.info("request "+receivedRequest.getTransactionID()+" enviado a DNS "+dnsCounter);
                    socket.send(dnsPacket);
                }else{
                    // ==== SI ES OTRO, ES RESPONSE. RECIBIR DE DNS ====

                    
                    
                    DNSResponse receivedResponse = formatAndGetDNSResponse(receivePacket.getData(), receivePacket.getLength());
                    receivedResponse.printDNSResponse();
                    System.out.println("\n\n\n");
                    
                    LOGGER.info("Se recibió response para el paquete "+receivedResponse.getTransactionID()+" del source: "+receivePacket.getAddress().toString());
                    
                    // byte[] receivedData = new byte[receivePacket.getLength()];
                    // byte[] receivedDataTemp = receivePacket.getData();

                    // for (int i = 0; i < receivePacket.getLength(); i++) {
                        // receivedData[i] = receivedDataTemp[i];
                    // }
                    
                    // byte[] receivedDataFixed = new byte[receivePacket.getLength()-1];

                    // boolean changed = false;
                    // if (receivedData[receivedData.length - 1] == (byte) 0xC0) {
                        // Handle the case when the last byte is 0xC0
                        // receivedDataFixed = Arrays.copyOf(receivedData, receivedData.length-1);
                        // LOGGER.info("receivedDataFixed CAMBIO: "+receivedDataFixed.length);
                        // changed = true;
                    // }else{
                        // LOGGER.info("receivedDataFixed SIN CAMBIO: "+receivedData.length);
                    // }

                    // AQUÍ GUARDAR EN MI CACHE
                    // AQUÍ GUARDAR EN MI CACHE
                    // AQUÍ GUARDAR EN MI CACHE
                    // AQUÍ GUARDAR EN MI CACHE

                    RequestInfo client = requesters.get(receivedResponse.getTransactionID());

                    if(client != null){
                        clientAddress = client.getAddress();
                        clientPort = client.getPort();
                        requesters.remove(receivedResponse.getTransactionID());
                        querysWithoutResponse--;
                    }else{
                        LOGGER.info("No se encontró el cliente para el ID de transacción: " + receivedResponse.getTransactionID());
                    }


                    // Envía la respuesta de vuelta al cliente
                    // if(changed){
                        // socket.send(new DatagramPacket(receivedDataFixed, receivedDataFixed.length,clientAddress,clientPort));
                    // }else{
                        // socket.send(new DatagramPacket(receivedData, receivedData.length,clientAddress,clientPort));
                        // }
                        
                    socket.send(new DatagramPacket(receivedResponse.getData(), receivedResponse.getLength(),clientAddress,clientPort));
                }


                
                // LOGGER.info("LEGIBLE: " + filterPrintableChars(receivePacket.getData(), receivePacket.getLength()));
                // LOGGER.info("HEXADECIMAL: " + bytesToHex(receivePacket.getData(),receivePacket.getLength()));
                // while (true) { 
                    


                    // byte[] receivedDNSData = new byte[1024];
                    // DatagramPacket receivedDNSPacket = new DatagramPacket(receivedDNSData, receivedDNSData.length);

                    // try {
                        // socket.setSoTimeout(500);
                        // // socket.receive(receivedDNSPacket);
                        // LOGGER.info("source del packet: "+receivedDNSPacket.getAddress().toString());
                        // while(receivedDNSPacket.getAddress().toString().equals("/127.0.0.1")){
                        //     System.out.println("DENTRO DEL WHILE. "+receivedDNSPacket.getAddress().toString());
                        //     socket.receive(receivedDNSPacket);
                        // }
                        // DNSResponse receivedResponse = formatAndGetDNSResponse(receivedDNSPacket.getData());
                        // LOGGER.info("HEXADECIMAL recibido: " + bytesToHex(receivedDNSPacket.getData(),receivedDNSPacket.getLength()));
                        // receivedResponse.printDNSResponse();
                        // System.out.println("\n\n\n");
                        // LOGGER.info("dnsPacketL2: "+receivedDNSPacket.getLength());


                        // // LOGGER.info("Respuesta del Proveedor - 2    : " + filterPrintableChars(dnsPacket.getData(),dnsPacket.getLength()));
                        // // LOGGER.info("Respuesta del Proveedor LENGTH: " + dnsPacket.getLength());
                        
                        // byte[] receivedData = new byte[receivedDNSPacket.getLength()];
                        // byte[] receivedDataTemp = receivedDNSPacket.getData();
                        // LOGGER.info("receivedData LENGTH: "+receivedData.length);
                        // LOGGER.info("receivedDataTemp LENGTH: "+receivedDataTemp.length);

                        // for (int i = 0; i < receivedDNSPacket.getLength(); i++) {
                        //     receivedData[i] = receivedDataTemp[i];
                        // }
                        
                        // LOGGER.info("receivedData LENGTH: "+receivedData.length);
                        // LOGGER.info("receivedData Respuesta: " + bytesToHex(receivedData,receivedData.length));

                        // byte[] receivedDataFixed = new byte[receivedDNSPacket.getLength()-1];

                        // boolean changed = false;
                        // if (receivedData[receivedData.length - 1] == (byte) 0xC0) {
                        //     // Handle the case when the last byte is 0xC0
                        //     receivedDataFixed = Arrays.copyOf(receivedData, receivedData.length-1);
                        //     LOGGER.info("receivedDataFixed CAMBIO: "+receivedDataFixed.length);
                        //     changed = true;
                        // }else{
                        //     LOGGER.info("receivedDataFixed SIN CAMBIO: "+receivedData.length);
                        // }

                        // // AQUÍ GUARDAR EN MI CACHE
                        // // AQUÍ GUARDAR EN MI CACHE
                        // // AQUÍ GUARDAR EN MI CACHE
                        // // AQUÍ GUARDAR EN MI CACHE
                        


                        // // Envía la respuesta de vuelta al cliente
                        // if(changed){
                        //     socket.send(new DatagramPacket(receivedDataFixed, receivedDataFixed.length,receivePacket.getAddress(),receivePacket.getPort()));
                        // }else{
                        //     socket.send(new DatagramPacket(receivedData, receivedData.length,receivePacket.getAddress(),receivePacket.getPort()));
                        // }
                        // break;
                    // } catch (SocketTimeoutException e) {
                        // System.out.println("Timeout esperando la respuesta del servidor DNS.");
                    // }
                // }
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout esperando la respuesta del servidor DNS.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%x ", bytes[i]));
        }
        return sb.toString().trim();
    }

    private static DNSRequest formatAndGetDNSRequest(byte[] data, int dataLength) {
        byte[] dataCopy = new byte[dataLength];
        System.arraycopy(data, 0, dataCopy, 0, dataLength);
        System.out.println("DENTRO DE formatAndGetDNSRequest, con data length: " + dataCopy.length + "y "+dataLength);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Gets all the header info
        short transactionID = buffer.getShort(0);
        short flags = buffer.getShort(2);
        short numQuestions = buffer.getShort(4);
        short numAnswerRRs = buffer.getShort(6);
        short numAuthorityRRs = buffer.getShort(8);
        short numAdditionalRRs = buffer.getShort(10);

        //Get Question info, starting from byte 12
        StringBuilder domainName = new StringBuilder();
        int position = 12; 
            //To get domain info:
        while (buffer.get(position) != 0) {
            int segmentLength = buffer.get(position); //Gets length of the next part of the domain
            position++;
            for (int i = 0; i < segmentLength; i++) { 
                domainName.append((char) buffer.get(position));
                position++;
            }
            domainName.append('.'); //Separates segments by point, building the domain name like dns.google.com
        }
        position++; //skips the \0 character
        if (domainName.length() > 0) {
            domainName.setLength(domainName.length() - 1); //Removes the last added point.
        }

        // Split the domain name 
        String[] domainNameParts = domainName.toString().split("\\.");
        int queryLabelCount = domainNameParts.length;

            //To get QType and QClass
        short queryType = buffer.getShort(position);
        short queryClass = buffer.getShort(position + 2);

        

        DNSRequest returnRequest = new DNSRequest(dataCopy, dataCopy.length, transactionID, flags, numQuestions, numAnswerRRs, numAuthorityRRs, numAdditionalRRs,
                                        domainName.toString(), domainName.toString().length(),domainNameParts, queryLabelCount, queryType, queryClass);
        return returnRequest;
    }


    private static DNSResponse formatAndGetDNSResponse(byte[] data, int dataLength) {
        byte[] dataCopy = new byte[dataLength];
        System.arraycopy(data, 0, dataCopy, 0, dataLength);
        System.out.println("DENTRO DE formatAndGetDNSResponse, con data: " + bytesToHex(dataCopy, dataCopy.length));
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Gets all the header info
        short transactionID = buffer.getShort(0);
        short flags = buffer.getShort(2);
        short numQuestions = buffer.getShort(4);
        short numAnswerRRs = buffer.getShort(6);
        short numAuthorityRRs = buffer.getShort(8);
        short numAdditionalRRs = buffer.getShort(10);

        //Get Question info, starting from byte 12
        StringBuilder domainName = new StringBuilder();
        int position = 12; 
            //To get domain info:
        while (buffer.get(position) != 0) {
            int segmentLength = buffer.get(position); //Gets length of the next part of the domain
            position++;
            for (int i = 0; i < segmentLength; i++) { 
                domainName.append((char) buffer.get(position));
                position++;
            }
            domainName.append('.'); //Separates segments by point, building the domain name like dns.google.com
        }
        position++; //skips the \0 character
        if (domainName.length() > 0) {
            domainName.setLength(domainName.length() - 1); //Removes the last added point.
        }

        // Split the domain name 
        String[] domainNameParts = domainName.toString().split("\\.");
        int queryLabelCount = domainNameParts.length;

        //To get QType and QClass
        short queryType = buffer.getShort(position);
        short queryClass = buffer.getShort(position + 2);
        position = position+4;

        

        DNSResponse returnResponse = new DNSResponse(dataCopy, dataCopy.length, transactionID, flags, numQuestions, numAnswerRRs, numAuthorityRRs, numAdditionalRRs,
                                        domainName.toString(), domainName.toString().length(),domainNameParts, queryLabelCount, queryType, queryClass);

        int countAnswers = (int)numAnswerRRs;
        int AnswerIndex = 0;

        while(countAnswers > 0){

            // ==== OBTENER URL de ANSWER: ====
            StringBuilder answerNameBuilder = new StringBuilder();
            String answerName = "";
            if(buffer.get(position) == (byte)0xC0){ //Si es comprimido, obtiene domain y salta a types
                System.out.println("SE ENCONTRÓ C0 en posición "+position);
                answerName = domainName.toString();
                position = position + 2;
            }else{
                while (buffer.get(position) != 0) {
                    int segmentLength = buffer.get(position); //Gets length of the next part of the domain
                    position++;
                    for (int i = 0; i < segmentLength; i++) { 
                        answerNameBuilder.append((char) buffer.get(position));
                        position++;
                    }
                    answerNameBuilder.append('.'); //Separates segments by point, building the domain name like dns.google.com
                }
                position++; //skips the \0 character
                if (answerNameBuilder.length() > 0) {
                    answerNameBuilder.setLength(answerNameBuilder.length() - 1); //Removes the last added point.
                }
                answerName = answerNameBuilder.toString();
            }

            
            //To get AType and AClass
            short ansType = buffer.getShort(position);
            short ansClass = buffer.getShort(position + 2);
            position += 4;
            
            System.out.println("type: "+ansType+" class: "+ansClass);
            int ansTTL = buffer.getInt(position);
            position += 4;
            System.out.println("position después de obtener ttl y ttl: "+position+" ttl: "+ansTTL);

            String ansAddress = "";
            if(ansType == (short)0x0001){
                short tempAddressLengthShort = buffer.getShort(position);
                int tempAddressLength = Short.toUnsignedInt(tempAddressLengthShort);
                System.out.println("es tipo A con length: "+tempAddressLength);
                position += 2;
                for (int i = 0; i < 4; i++) {
                    int unsignedValue = buffer.get(position) & 0xFF;
                    ansAddress += unsignedValue + ".";
                    //ansAddress += Short.toUnsignedInt(buffer.get(position)) + ".";
                    position++;
                }
                ansAddress = ansAddress.substring(0, ansAddress.length() - 1);
            }else if(ansType == (short)0x0005){ //CNAME
                int tempAddressLength = Short.toUnsignedInt(buffer.getShort(position));
                System.out.println("es tipo CNAME con length: "+tempAddressLength);
                position += 2;

                for (int i = 0; i < tempAddressLength;) {
                    if(buffer.get(position) == (byte)0xC0){ //En el CNAME viene comprimido
                        position++;
                        i++;
                        int tempPosition = buffer.get(position) & 0xFF;//Saltar a inicio de compresion

                        if(buffer.get(tempPosition) == (byte)0x00){ //Se acabó nombre en compresión
                            ansAddress += ".";
                            position++;
                            i++;
                            break;
                        }else{
                            int tempSegmentLength = buffer.get(tempPosition) & 0xFF;
                            for(int j=0;j<tempSegmentLength;j++){
                                ansAddress += (char) buffer.get(tempPosition);
                                tempPosition++;
                            }
                        }
                    }else{ //No viene comprimido
                        int tempSegmentLength = buffer.get(position) & 0xFF;
                        position++;
                        i++;
                        for(int j=0;j<tempSegmentLength;j++){
                            ansAddress += (char) buffer.get(position);
                            position++;
                            i++;
                        }
                        ansAddress += ".";
                    }
                }
                ansAddress = ansAddress.substring(0, ansAddress.length() - 1);
            }

            returnResponse.addAnswer(AnswerIndex, answerName, ansType, ansClass, ansTTL, ansAddress.split("\\.").length, ansAddress);
            countAnswers--;
        }
        return returnResponse;
    }


    public static int getQueryType(byte[] query) {
        ByteBuffer buffer = ByteBuffer.wrap(query);
        int position = getDomainNameLength(buffer) + 12;
        buffer.position(position);
        return buffer.getShort() & 0xFFFF;
    }

    private static int getDomainNameLength(ByteBuffer buffer) {
        int length = 0;
        int labelLength;

        while ((labelLength = buffer.get()) != 0) {
            length += labelLength + 1;
            buffer.position(buffer.position() + labelLength);
        }
        return length + 1;
    }

    private static String extractDomainName(byte[] query) {
        StringBuilder domainName = new StringBuilder();
        int position = 12;
        while (query[position] != 0) {
            int length = query[position++];
            for (int i = 0; i < length; i++) {
                domainName.append((char) query[position++]);
            }
            domainName.append('.'); // www.google.com
        }
        if (domainName.length() > 0) {
            domainName.setLength(domainName.length() - 1);
        }
        return domainName.toString();
    }


}
