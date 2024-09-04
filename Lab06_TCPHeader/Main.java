
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


    private int serverSequence = 0;
    private int clientSequence = 0;


    private static String textDonQuijote = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor. Una olla de algo más vaca que carnero, salpicón las más noches, duelos y quebrantos los sábados, lentejas los viernes, algún palomino de añadidura los domingos, consumían las tres partes de su hacienda. El resto della concluían sayo de velarte, calzas de velludo para las fiestas con sus pantuflos de lo mismo, los días de entre semana se honraba con su vellori de lo más fino. Tenía en su casa una ama que pasaba de los cuarenta, y una sobrina que no llegaba a los veinte, y un mozo de campo y plaza, que así ensillaba el rocín como tomaba la podadera. Frisaba la edad de nuestro hidalgo con los cincuenta años, era de complexión recia, seco de carnes, enjuto de rostro; gran madrugador y amigo de la caza. Quieren decir que tenía el sobrenombre de Quijada o Quesada (que en esto hay alguna diferencia en los autores que deste caso escriben), aunque por conjeturas verosímiles se deja entender que se llama Quijana; pero esto importa poco a nuestro cuento; basta que en la narración dél no se salga un punto de la verdad.";
    private static String textLosMosqueteros = "El primer lunes del mes de abril de 1625, el burgo de Meung, donde nació el autor del Roman de la Rose, parecía estar en una revolución tan completa como si los hugonotes hubieran venido a hacer de ella una segunda Rochelle. Muchos burgueses, al ver huir a las mujeres por la calle Mayor, al oír gritar a los niños en el umbral de las puertas, se apresuraban a endosarse la coraza y, respaldando su aplomo algo incierto con un mosquete o una partesana, se dirigían hacia la hostería del Franc Meunier, ante la cual bullía, creciendo de minuto en minuto, un grupo compacto, ruidoso y lleno de curiosidad. En ese tiempo los pánicos eran frecuentes, y pocos días pasaban sin que una aldea a otra registrara en sus archivos algún acontecimiento de ese género. Estaban los señores que guerreaban entre sí; estaba el rey que hacía la guerra al cardenal; estaba el Español que hacía la guerra al rey. Luego, además de estas guerras sordas o públicas, secretas o patentes, estaban los ladrones, los mendigos, los hugonotes, los lobos y los lacayos que hacían la guerra a todo el mundo. Los burgueses se armaban siempre contra los ladrones, contra los lobos, contra los lacayos, con frecuencia contra los señores y los hugonotes, algunas veces contra el rey, pero nunca contra el cardenal ni contra el Español. De este hábito adquirido resulta, pues, que el susodicho primer lunes del mes de abril de 1625, los burgueses, al oír el barullo y no ver ni el banderín amarillo y rojo ni la librea del duque de Richelieu, se precipitaron hacia la hostería del Franc Meunier.";



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
            int windowSize = 65535;
            int serverPort = 80;
            int clientPort = new Random().nextInt(900) + 100; // Random entre 100 y 999
            int clientInitialSequence = new Random().nextInt(200001) + 100000; // Random entre 100000 y 300000
            int serverInitialSequence = new Random().nextInt(200001) + 500000; // Random entre 500000 y 700000
            int packetLossProbability = 0;
            String dataTransmissionDirection = "ambos";

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
            }}catch(Exception e){
                System.out.println("Valor inválido. Se usará el valor por defecto.");
            }

            System.out.print("Numero de Secuencia Inicial del Servidor (Default: entre 500000 y 700000): ");
            input = scanner.nextLine();
            try{if (!input.isEmpty()) {
                serverInitialSequence = Integer.parseInt(input);
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
                System.out.println("Dirección de transmisión de datos inválida. Intente nuevamente.");
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
            }

            System.out.println("\n\n==Valores configurados:==");
            System.out.println("Window Size: " + windowSize);
            System.out.println("Puerto del Servidor: " + serverPort);
            System.out.println("Puerto del Cliente: " + clientPort);
            System.out.println("Secuencia Inicial del Cliente: " + clientInitialSequence);
            System.out.println("Secuencia Inicial del Servidor: " + serverInitialSequence);
            System.out.println("Probabilidad de pérdida de paquetes: " + packetLossProbability + "%");
            System.out.println("Dirección de transmisión: " + dataTransmissionDirection);





            // Inicialización de servidor y cliente
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

            LOGGER.info("Textos de Don Quijote: ");
            for (String texto : textosDonQuijote) {
                LOGGER.info(" (" + texto.length() + " caracteres) : " + texto);
            }
            LOGGER.info("MODA de longitud de palabras: " + getLengthMode(textosDonQuijote));






        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Falta un parámetro!");
        } catch (SocketException e) {
            System.out.println("Se perdió la conexión! ");
        } catch (Exception e) {
            System.out.println("Ocurrió un error inesperado ("+e.getClass().getSimpleName()+"). Saliendo. ");
            // System.out.println("\nOcurrió un error inesperado ("+e+"). Saliendo. ");
        }
    }





    // Clase Packet para guardar la información de cada paquete enviado/recibido
    class Packet {
        private int id;
        private String status;
        private String sender;
        private int sourcePort;
        private int destinationPort;
        private List<String> flags;
        private int sequenceNumber;
        private int ackNumber;
        private int length;
        private int windowSize;

        public Packet(int id, String status, String sender, int sourcePort, int destinationPort,
                      List<String> flags, int sequenceNumber, int ackNumber, int length, int windowSize) {
            this.id = id;
            this.status = status;
            this.sender = sender;
            this.sourcePort = sourcePort;
            this.destinationPort = destinationPort;
            this.flags = flags;
            this.sequenceNumber = sequenceNumber;
            this.ackNumber = ackNumber;
            this.length = length;
            this.windowSize = windowSize;
        }

        // Getters and setters for all fields
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }

        public int getSourcePort() { return sourcePort; }
        public void setSourcePort(int sourcePort) { this.sourcePort = sourcePort; }

        public int getDestinationPort() { return destinationPort; }
        public void setDestinationPort(int destinationPort) { this.destinationPort = destinationPort; }

        public List<String> getFlags() { return flags; }
        public void setFlags(List<String> flags) { this.flags = flags; }

        public int getSequenceNumber() { return sequenceNumber; }
        public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

        public int getAckNumber() { return ackNumber; }
        public void setAckNumber(int ackNumber) { this.ackNumber = ackNumber; }

        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }

        public int getWindowSize() { return windowSize; }
        public void setWindowSize(int windowSize) { this.windowSize = windowSize; }
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
