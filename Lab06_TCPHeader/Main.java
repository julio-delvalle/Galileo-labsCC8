
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;


public class Main  {


    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static String logsFolder = "logs";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
    private static Integer nThreadServer;


    private static int currentPacketId = 0;
    private static int serverSequence = 0;
    private static int clientSequence = 0;
    private static int clientPort = 0;
    private static int serverPort = 80;
    private static int windowSize = 65535;
    private static int packetLossProbability = 0;
    private static String dataTransmissionDirection = "ambos";



    private static String textDonQuijote = """
                                           En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor. Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lantejas los viernes, algún palomino de añadidura los domingos, consumían las tres partes de su hacienda. El resto della concluían sayo de velarte, calzas de velludo para las fiestas, con sus pantuflos de lo mesmo, y los días de entresemana se honraba con su vellorí de lo más fino. Tenía en su casa una ama que pasaba de los cuarenta, y una sobrina que no llegaba a los veinte, y un mozo de campo y plaza, que así ensillaba el rocín como tomaba la podadera. Frisaba la edad de nuestro hidalgo con los cincuenta años; era de complexión recia, seco de carnes, enjuto de rostro, gran madrugador y amigo de la caza. Quieren decir que tenía el sobrenombre de Quijada, o Quesada, que en esto hay alguna diferencia en los autores que deste caso escriben; aunque por conjeturas verosímiles se deja entender que se llamaba Quijana. Pero esto importa poco a nuestro cuento: basta que en la narración dél no se salga un punto de la verdad.
                                            Es, pues, de saber que este sobredicho hidalgo, los ratos que estaba ocioso, que eran los más del año, se daba a leer libros de caballerías, con tanta afición y gusto, que olvidó casi de todo punto el ejercicio de la caza, y aun la administración de su hacienda; y llegó a tanto su curiosidad y desatino en esto, que vendió muchas hanegas de tierra de sembradura para comprar libros de caballerías en que leer, y así, llevó a su casa todos cuantos pudo haber dellos; y de todos, ningunos le parecían tan bien como los que compuso el famoso Feliciano de Silva; porque la claridad de su prosa y aquellas entricadas razones suyas le parecían de perlas, y más cuando llegaba a leer aquellos requiebros y cartas de desafíos, donde en muchas partes hallaba escrito: «La razón de la sinrazón que a mi razón se hace, de tal manera mi razón enflaquece, que con razón me quejo de la vuestra fermosura». Y también cuando leía: «... los altos cielos que de vuestra divinidad divinamente con las estrellas os fortifican, y os hacen merecedora del merecimiento que merece la vuestra grandeza».""";
    private static String textLosMosqueteros = "El primer lunes del mes de abril de 1625, el burgo de Meung, donde nació el autor del Roman de la Rose, parecía estar en una revolución tan completa como si los hugonotes hubieran venido a hacer de ella una segunda Rochelle. Muchos burgueses, al ver huir a las mujeres por la calle Mayor, al oír gritar a los niños en el umbral de las puertas, se apresuraban a endosarse la coraza y, respaldando su aplomo algo incierto con un mosquete o una partesana, se dirigían hacia la hostería del Franc Meunier, ante la cual bullía, creciendo de minuto en minuto, un grupo compacto, ruidoso y lleno de curiosidad. En ese tiempo los pánicos eran frecuentes, y pocos días pasaban sin que una aldea a otra registrara en sus archivos algún acontecimiento de ese género. Estaban los señores que guerreaban entre sí; estaba el rey que hacía la guerra al cardenal; estaba el Español que hacía la guerra al rey. Luego, además de estas guerras sordas o públicas, secretas o patentes, estaban los ladrones, los mendigos, los hugonotes, los lobos y los lacayos que hacían la guerra a todo el mundo. Los burgueses se armaban siempre contra los ladrones, contra los lobos, contra los lacayos, con frecuencia contra los señores y los hugonotes, algunas veces contra el rey, pero nunca contra el cardenal ni contra el Español. De este hábito adquirido resulta, pues, que el susodicho primer lunes del mes de abril de 1625, los burgueses, al oír el barullo y no ver ni el banderín amarillo y rojo ni la librea del duque de Richelieu, se precipitaron hacia la hostería del Franc Meunier.";
    
    private static String textDonQuijoteCorto = """
                                           En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que viv\u00eda un hidalgo de los de lanza en astillero, adarga antigua, roc\u00edn flaco y galgo corredor. Una olla de algo m\u00e1s vaca que carnero, salpic\u00f3n las m\u00e1s noches, duelos y quebrantos los s\u00e1bados, lentejas los viernes, alg\u00fan palomino de a\u00f1adidura los domingos, consum\u00edan las tres partes de su hacienda. El resto della conclu\u00edan sayo de velarte, calzas de velludo para las fiestas con sus pantuflos de lo mismo, los d\u00edas de entre semana se honraba con su vellori de lo m\u00e1s fino.""";
    private static String textLosMosqueterosCorto = "El primer lunes del mes de abril de 1625, el burgo de Meung, donde nació el autor del Roman de la Rose, parecía estar en una revolución tan completa como si los hugonotes hubieran venido a hacer de ella una segunda Rochelle. Muchos burgueses, al ver huir a las mujeres por la calle Mayor, al oír gritar a los niños en el umbral de las puertas, se apresuraban a endosarse la coraza y, respaldando su aplomo algo incierto con un mosquete o una partesana, se dirigían hacia la hostería del Franc Meunier, ante la cual bullía, creciendo de minuto en minuto, un grupo compacto, ruidoso y lleno de curiosidad. ";



    public static void main(String[] args) throws IOException{
        try {


            // DEFINICION DE LOGGER:
            Logger LOGGER = Logger.getAnonymousLogger();
            LOGGER.setUseParentHandlers(false);
            Files.createDirectories(Paths.get(logsFolder));
            FileHandler fileHandler = new FileHandler(logsFolder + File.separator + "T"+ nThreadServer + "-" + dateFormat.format((new Date()).getTime()) + ".log");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            fileHandler.setFormatter(new FormatterWebServer());
            consoleHandler.setFormatter(new FormatterWebServer());
            LOGGER.addHandler(fileHandler);
            LOGGER.addHandler(consoleHandler);


            System.out.println("\n\n======== LABORATORIO 06 ========");
            System.out.println("Ingrese los siguientes parámetros: ");
            System.out.println(" * Puede presionar ENTER para valor Default.\n");
            // Default values
            windowSize = 65535;
            serverPort = 80;
            clientPort = new Random().nextInt(900) + 100; // Random entre 100 y 999
            int clientInitialSequence = new Random().nextInt(200001) + 100000; // Random entre 100000 y 300000
            int serverInitialSequence = new Random().nextInt(200001) + 500000; // Random entre 500000 y 700000
            boolean useShortTexts = false;

            Scanner scanner = new Scanner(System.in);

            System.out.print("Valor del Window Size Inicial (Default: 65535): ");
            String input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                windowSize = Integer.parseInt(input);
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.print("Puerto del Servidor (Default: 80): ");
            input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                serverPort = Integer.parseInt(input);
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.print("Puerto del Cliente (Default: entre 100 y 999): ");
            input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                clientPort = Integer.parseInt(input);
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.print("Número de Secuencia Inicial del Cliente (Default: entre 100000 y 300000): ");
            input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                clientInitialSequence = Integer.parseInt(input);
                clientSequence = clientInitialSequence;
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.print("Numero de Secuencia Inicial del Servidor (Default: entre 500000 y 700000): ");
            input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                serverInitialSequence = Integer.parseInt(input);
                serverSequence = serverInitialSequence;
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.print("Probabilidad de perdida de paquetes (0-99%, Default: 0%): ");
            input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                packetLossProbability = Integer.parseInt(input);
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.print("Dirección de transmisión de datos \nC -> cliente\nS -> servidor\nA -> ambos\nDefault: ambos\n: ");
            input = scanner.nextLine();
            if (!input.isEmpty()) {
                dataTransmissionDirection = input.toLowerCase();
                switch(dataTransmissionDirection){
                    case "c":
                    case "C":
                        dataTransmissionDirection = "cliente";
                        break;
                    case "s":
                    case "S":
                        dataTransmissionDirection = "servidor";
                        break;
                    case "a":
                    case "A":
                        dataTransmissionDirection = "ambos";
                        break;
                }
            }
            while(!"cliente".equals(dataTransmissionDirection) && !"servidor".equals(dataTransmissionDirection) && !"ambos".equals(dataTransmissionDirection)){
                System.out.println("Dirección de transmisión de datos inválida. Intente nuevamente.\nC -> cliente\nS -> servidor\nA -> ambos\nDefault: ambos\n: ");
                input = scanner.nextLine();
                if (!input.isEmpty()) {
                    dataTransmissionDirection = input.toLowerCase();
                    switch(dataTransmissionDirection){
                        case "c":
                        case "C":
                            dataTransmissionDirection = "cliente";
                            break;
                        case "s":
                        case "S":
                            dataTransmissionDirection = "servidor";
                            break;
                        case "a":
                        case "A":
                            dataTransmissionDirection = "ambos";
                            break;
                    }
                }else{
                    dataTransmissionDirection = "ambos";
                }
            }

            System.out.print("Usar textos cortos (S/N) (Default: N): ");
            input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                if(input.toLowerCase().equals("n")){
                    useShortTexts = false;
                }else if(input.toLowerCase().equals("s")){
                    useShortTexts = true;
                    textDonQuijote = textDonQuijoteCorto;
                    textLosMosqueteros = textLosMosqueterosCorto;
                }else{
                    useShortTexts = false;
                    System.out.println("Valor inválido. Se usará el valor por defecto.");
                }
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.println("\n\n==Valores configurados:==");
            System.out.println("Window Size: " + windowSize);
            System.out.println("Puerto del Servidor: " + serverPort);
            System.out.println("Puerto del Cliente: " + clientPort);
            System.out.println("Secuencia Inicial del Cliente: " + clientInitialSequence);
            System.out.println("Secuencia Inicial del Servidor: " + serverInitialSequence);
            System.out.println("Probabilidad de pérdida de paquetes: " + packetLossProbability + "%");
            System.out.println("Dirección de transmisión: " + dataTransmissionDirection);
            System.out.println("Usar textos cortos: " + useShortTexts);


            System.out.println("\nPresione ENTER para continuar...");
            scanner.nextLine();




            // Inicialización de servidor y cliente

            // Dividir el texto de Don Quijote en textos más pequeños
            ArrayList<String> textosDonQuijote = new ArrayList<>();
            while(textDonQuijote.length() != 0){
                int randomSplit = new Random().nextInt(9) + 3; //Don Quijote dividido en 3 a 12 espacios

                int nthSpaceIndex = -1;
                int spaceCount = 0;
                for (int i = 0; i < textDonQuijote.length(); i++) {
                    if (textDonQuijote.charAt(i) == ' ') {
                        spaceCount++;
                        if (spaceCount == randomSplit) {
                            nthSpaceIndex = i;
                            break;
                        }
                    }
                }
                
                // Si se encontró el espacio RandomSplit (nth), se divide el texto
                if (nthSpaceIndex != -1) {
                    String textoSplit = textDonQuijote.substring(0, nthSpaceIndex);
                    String textoRestante = textDonQuijote.substring(nthSpaceIndex + 1);
                    
                    //Quitar el textoSplit a textDonQuijote
                    textDonQuijote = textoRestante;
                    
                    //Guardar el textoSplit en el ArrayList
                    textosDonQuijote.add(textoSplit);
                } else {
                    // Si ya no hay espacios, guardar el texto restante en el ArrayList
                    textosDonQuijote.add(textDonQuijote);
                    textDonQuijote = "";
                }
            }

            LOGGER.info("Textos de Don Quijote: "+textosDonQuijote.size());
            // for (String texto : textosDonQuijote) {
            //     LOGGER.info(" (" + texto.length() + " caracteres) : " + texto);
            // }
            LOGGER.info("MODA de longitud de palabras Don Quijote: " + getLengthMode(textosDonQuijote));


            



            // Dividir el texto de Los Mosqueteros en textos más pequeños
            ArrayList<String> textosLosMosqueteros = new ArrayList<>();
            while(textLosMosqueteros.length() != 0){
                int randomSplit = new Random().nextInt(10) + 2; //Los mosqueteros dividido en 2 a 12 espacios

                int nthSpaceIndex = -1;
                int spaceCount = 0;
                for (int i = 0; i < textLosMosqueteros.length(); i++) {
                    if (textLosMosqueteros.charAt(i) == ' ') {
                        spaceCount++;
                        if (spaceCount == randomSplit) {
                            nthSpaceIndex = i;
                            break;
                        }
                    }
                }
                
                // Si se encontró el espacio RandomSplit (nth), se divide el texto
                if (nthSpaceIndex != -1) {
                    String textoSplit = textLosMosqueteros.substring(0, nthSpaceIndex);
                    String textoRestante = textLosMosqueteros.substring(nthSpaceIndex + 1);
                    
                    //Quitar el textoSplit a textLosMosqueteros
                    textLosMosqueteros = textoRestante;
                    
                    //Guardar el textoSplit en el ArrayList
                    textosLosMosqueteros.add(textoSplit);
                } else {
                    // Si ya no hay espacios, guardar el texto restante en el ArrayList
                    textosLosMosqueteros.add(textLosMosqueteros);
                    textLosMosqueteros = "";
                }
            }

            LOGGER.info("Textos de Los Mosqueteros: "+textosLosMosqueteros.size());
            // for (String texto : textosLosMosqueteros) {
            //     LOGGER.info(" (" + texto.length() + " caracteres) : " + texto);
            // }
            LOGGER.info("MODA de longitud de palabras Los mosqueteros: " + getLengthMode(textosLosMosqueteros));







            //  =================== FIN DE LA INICIALIZACION ===================
            
            //  =================== ENVÍO DE PAQUETES ===================
            clientSequence = clientInitialSequence;
            serverSequence = serverInitialSequence;


            //  =========== HANDSHAKE ===========
            excecuteAndPrintHandshake();
            
            


            // while(textosDonQuijote.size() > 0 || textosLosMosqueteros.size() > 0){
            // }




        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Falta un parámetro!");
        } catch (SocketException e) {
            System.out.println("Se perdió la conexión! ");
        } catch (Exception e) {
            System.out.println("Ocurrió un error inesperado ("+e.getClass().getSimpleName()+"). Saliendo. ");
            // System.out.println("\nOcurrió un error inesperado ("+e+"). Saliendo. ");
        }
    }





    


    static void excecuteAndPrintHandshake(){
        Packet packetToPrint = new Packet(currentPacketId++, "succeed", "Client", clientPort, serverPort, Arrays.asList("SYN"), clientSequence, 0, 0, windowSize, "");
        packetToPrint.printPacketHeader();
        packetToPrint.sendAndPrintPacket();
        clientSequence++;
        packetToPrint = new Packet(currentPacketId++, "succeed", "Server", clientPort, serverPort, Arrays.asList("SYN", "ACK"), clientSequence, serverSequence, 0, windowSize, "");
        packetToPrint.sendAndPrintPacket();
        serverSequence++;
        packetToPrint = new Packet(currentPacketId++, "succeed", "Client", clientPort, serverPort, Arrays.asList("ACK"), clientSequence, serverSequence, 0, windowSize, "");
        packetToPrint.sendAndPrintPacket();
    }

    public static int getLengthMode(ArrayList<String> wordsList){
    HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
    int max  = 1;
        int temp = 0;

        for(int i = 0; i < wordsList.size(); i++) {

            if (hm.get(wordsList.get(i).length()) != null) {

                int count = hm.get(wordsList.get(i).length());
                count++;
                hm.put(wordsList.get(i).length(), count);

                if(count > max) {
                    max  = count;
                    temp = wordsList.get(i).length();
                }
            }else 
                hm.put(wordsList.get(i).length(),1);
            }
        return temp;
    }
}


// Clase Packet para guardar la información de cada paquete enviado/recibido
class Packet {
    private int id;
    private String status;
    private String sender;
    private int clientPort;
    private int serverPort;
    private List<String> flags;
    private int clientSequenceNumber;
    private int serverSequenceNumber;
    private int length;
    private int windowSize;
    private String data;

    public Packet(int id, String status, String sender, int clientPort, int serverPort,
                  List<String> flags, int clientSequenceNumber, int serverSequenceNumber, int length, int windowSize, String data) {
        this.id = id;
        this.status = status;
        this.sender = sender;
        this.clientPort = clientPort;
        this.serverPort = serverPort;
        this.flags = flags;
        this.clientSequenceNumber = clientSequenceNumber;
        this.serverSequenceNumber = serverSequenceNumber;
        this.length = length;
        this.windowSize = windowSize;
        this.data = data;
    }

    // Getters and setters for all fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public int getClientPort() { return clientPort; }
    public void setClientPort(int clientPort) { this.clientPort = clientPort; }

    public int getServerPort() { return serverPort; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }

    public int getClientSequenceNumber() { return clientSequenceNumber; }
    public void setClientSequenceNumber(int clientSequenceNumber) { this.clientSequenceNumber = clientSequenceNumber; }

    public int getServerSequenceNumber() { return serverSequenceNumber; }
    public void setServerSequenceNumber(int serverSequenceNumber) { this.serverSequenceNumber = serverSequenceNumber; }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public int getWindowSize() { return windowSize; }
    public void setWindowSize(int windowSize) { this.windowSize = windowSize; }


public void sendAndPrintPacket() {
    if(sender.equals("Client")){
        System.out.printf("%-5d | %-10s | %-10s | %-10d | %-10d | %-15s | %-12d | %-12d | %-5d | %-8d | %s%n",
        id,
        status,
        sender,
        clientPort,
        serverPort,
        String.join(",", flags),
        clientSequenceNumber,
        serverSequenceNumber,
        length,
        windowSize,
        data.length() > 7 ? data.substring(0, 7)+"..." : data
        );
    }else{
        System.out.printf("%-5d | %-10s | %-10s | %-10d | %-10d | %-15s | %-12d | %-12d | %-5d | %-8d | %s%n",
        id,
        status,
        sender,
        serverPort,
        clientPort,
        String.join(",", flags),
        serverSequenceNumber,
        clientSequenceNumber,
        length,
        windowSize,
        data.length() > 7 ? data.substring(0, 7)+"..." : data
        );
    }
}

public static void printPacketHeader() {
    System.out.printf("%-5s | %-10s | %-10s | %-10s | %-10s | %-15s | %-12s | %-12s | %-5s | %-8s | %s%n",
        "Index", "Status", "Sender", "Src Port", "Dst Port", "Flags", "Seq Number", "Ack Number", "Len", "Window", "Data"
    );
}


}