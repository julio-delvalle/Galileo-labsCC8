import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.logging.Logger;

public class ThreadServer implements Runnable {
    private Integer nThreadServer;

    byte[] input = new byte[65535];
    private Integer delay;
    private DatagramSocket socket;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
    static Logger LOGGER = Logger.getLogger("Server");

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

    public ThreadServer(Integer id, DatagramSocket socket, Integer delay) {
        this.nThreadServer = id;
        this.socket = socket;
        this.delay = delay;

        listIPv4.put("www.youtube.com", "142.250.217.238");
        listIPv4.put("www.google.com", "8.8.8.8");
    }

    @Override
    public void run() {
        byte[] input = new byte[65535];
        DatagramPacket receivePacket = null;
        while (true) {
            try {
                LOGGER.info("("+nThreadServer+") > Thread waiting for new client....");
                receivePacket = new DatagramPacket(input, input.length);
                socket.receive(receivePacket);
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String receiveMessage = new String(receivePacket.getData());



                LOGGER.info("LEGIBLE: " + filterPrintableChars(receivePacket.getData(), receivePacket.getLength()));
                LOGGER.info("HEXADECIMAL: " + bytesToHex(receivePacket.getData(),receivePacket.getLength()));
                // LOGGER.info("Consulta DNS al dominio: " + extractDomainName(receivePacket.getData()));



                


                // InetAddress dnsServer = InetAddress.getByName(DNS_SERVERS[0]);
                // DatagramPacket dnsPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), dnsServer,
                //         53);
                // socket.send(dnsPacket);

                // socket.setSoTimeout(2000);
                // try {
                //     socket.receive(receivePacket);
                //     LOGGER.info("Respuesta del Proveedor: " + bytesToHex(receivePacket.getData(),receivePacket.getLength()));
                //     LOGGER.info("Respuesta del Proveedor - 2    : " + filterPrintableChars(receivePacket.getData(),receivePacket.getLength()));
                //     // Envía la respuesta de vuelta al cliente

                    // ESTO NO TODAVÍA!!!
                //     socket.send(new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),receivePacket.getAddress(),receivePacket.getPort()));
                // } catch (SocketTimeoutException e) {
                //     System.out.println("Timeout esperando la respuesta del servidor DNS.");
                // }
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

    private static String filterPrintableChars(byte[] data, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) data[i];
            if (c >= 32 && c <= 126) { // Imprimible ASCII range
                sb.append(c);
            } else {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    //    Crear un cliente udp, -- iterativo: que primero mande a preguntar a los root A-M, esto regresa un TLD, que es
//    el servidor que ya sabemos quien tiene la información que estamos pidiendo
    private static void iterative(byte[] query, int length) throws IOException {
        int port = 53;
        DatagramSocket socket = new DatagramSocket(port);
        byte[] input = new byte[65535];
        DatagramSocket ds = new DatagramSocket();
        InetAddress ip = InetAddress.getLocalHost();
        try {
            DatagramPacket packet = new DatagramPacket(query, length, InetAddress.getByName("198.41.0.4"), port);
            ds.send(packet);

            byte[] respuesta = new byte[65535];
            DatagramPacket receivePacket = new DatagramPacket(respuesta, respuesta.length);

            ds.receive(receivePacket);

            String respuestaS = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("> a.root [" + LocalDate.now() + " " + LocalTime.now() + "] UDP: " + respuestaS);
            ds.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static byte[] handleQuery(byte[] query, int queryLength) {
        // Formar una respuesta - Algunos campos son iguales a la consulta

        byte[] response = new byte[512];

        // Copiar el identificador de la consulta a la respuesta
        response[0] = query[0];
        response[1] = query[1];

        // Flags: Respuesta estándar, no autoritativa, no truncada, sin errores
        response[2] = (byte) 0x81; // QR=1, OPCODE=0000, AA=1, TC=0, RD=0
        response[3] = (byte) 0x80; // RA=1, Z=000, RCODE=0000

        // Número de preguntas: 1
        response[4] = 0x00;
        response[5] = 0x01;

        // Número de respuestas: 1
        response[6] = 0x00;
        response[7] = 0x01;

        // Número de registros de autoridad: 0
        response[8] = 0x00;
        response[9] = 0x00;

        // Número de registros adicionales: 0
        response[10] = 0x00;
        response[11] = 0x00;

        // Copiar la sección de preguntas desde la consulta a la respuesta
        System.arraycopy(query, 12, response, 12, queryLength - 12);

        int responseIndex = queryLength;

        // Respuesta: Puntero al nombre de dominio (0xc00c)
        response[responseIndex++] = (byte) 0xc0;
        response[responseIndex++] = 0x0c;

        // Tipo de registro: A (0x0001)
        response[responseIndex++] = 0x00;
        response[responseIndex++] = 0x01;

        // Clase: IN (0x0001)
        response[responseIndex++] = 0x00;
        response[responseIndex++] = 0x01;

        // TTL (Time to Live): 300 segundos (0x0000012c)
        response[responseIndex++] = 0x00;
        response[responseIndex++] = 0x00;
        response[responseIndex++] = 0x01;
        response[responseIndex++] = 0x2c;

        // Longitud de datos: 4 bytes (IPv4 address)
        response[responseIndex++] = 0x00;
        response[responseIndex++] = 0x04;

        // Dirección IP (por ejemplo, 192.168.1.1)
        String domainName = extractDomainName(query);
//      Si se encuentra dentro de mis registros
//        Dependiendo del QTYPE será la lista en la que lo va a buscar
        int qType = getQueryType(query);
        String strResult = "";

        switch (qType) {
            case A:
                strResult = listIPv4.get(domainName);
                break;
            case CNAME:
                strResult = listCNAME.get(domainName);
                break;
            case TXT:
                strResult = listTXT.get(domainName);
                break;
            case AAAA:
                strResult = listIPv6.get(domainName);
                break;
        }

        if (!strResult.equals("")){

        }else{

//          1. Root Server

//          2. TLD

//          3. Autoritativo

//          4. Almacenarlo en hashmap de addresses
            response[responseIndex++] = (byte) 8;
            response[responseIndex++] = (byte) 8;
            response[responseIndex++] = 8;
            response[responseIndex++] = 8;
        }
        return response;
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
            domainName.append('.');
        }
        if (domainName.length() > 0) {
            domainName.setLength(domainName.length() - 1);
        }
        return domainName.toString();
    }


}
