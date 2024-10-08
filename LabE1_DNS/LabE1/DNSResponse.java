
import java.util.ArrayList;

public class DNSResponse {
    private byte[] data;
    // DNS Response Header
    private short transactionID;
    private short flags;
    private short numQuestions;
    private short numAnswerRRs;
    private short numAuthorityRRs;
    private short numAdditionalRRs;

    // Query information
    private String queryName;
    private int queryNameLength;
    private String[] queryNameParts;
    private int queryLabelCount;
    private short queryType;
    private short queryClass;

    // Answer information
    private ArrayList<Answer> answers;

    public static class Answer {
        private String name;
        private short type;
        private short dnsClass;
        private int ttl;
        private int dataLength;
        private String address;

        // Constructor
        public Answer(String name, short type, short dnsClass, int ttl, int dataLength, String address) {
            this.name = name;
            this.type = type;
            this.dnsClass = dnsClass;
            this.ttl = ttl;
            this.dataLength = dataLength;
            this.address = address;
        }

        // Getters and setters
        // ... (implement as needed)
    }



    // DNSResponse Constructor
    public DNSResponse(byte[] data, int dataLength, short transactionID, short flags, short numQuestions, short numAnswerRRs,
                       short numAuthorityRRs, short numAdditionalRRs, String queryName,
                       int queryNameLength, String[] queryNameParts, int queryLabelCount, short queryType, short queryClass) {
        this.transactionID = transactionID;
        this.flags = flags;
        this.numQuestions = numQuestions;
        this.numAnswerRRs = numAnswerRRs;
        this.numAuthorityRRs = numAuthorityRRs;
        this.numAdditionalRRs = numAdditionalRRs;
        this.queryName = queryName;
        this.queryNameLength = queryNameLength;
        this.queryNameParts = queryNameParts;
        this.queryLabelCount = queryLabelCount;
        this.queryType = queryType;
        this.queryClass = queryClass;
        this.answers = new ArrayList<Answer>();
        this.data = new byte[dataLength];
        System.arraycopy(data, 0, this.data, 0, dataLength);
    }

    // Method to add an answer
    public void addAnswer(int index, String name, short type, short dnsClass, int ttl, int dataLength, String address) {
        answers.add(new Answer(name, type, dnsClass, ttl, dataLength, address));
    }


    // Method to set DNS response header information
    public void setDNSResponseHeader(short transactionID, short flags, short numQuestions, 
                                     short numAnswerRRs, short numAuthorityRRs, short numAdditionalRRs) {
        this.transactionID = transactionID;
        this.flags = flags;
        this.numQuestions = numQuestions;
        this.numAnswerRRs = numAnswerRRs;
        this.numAuthorityRRs = numAuthorityRRs;
        this.numAdditionalRRs = numAdditionalRRs;
        
        // Reinitialize the answers array based on the new number of answer RRs
        this.answers = new ArrayList<Answer>();
    }


    // Method to print DNS response in the specified format
    public void printDNSResponse() {
        System.out.println("DNS RESPONSE: ===========");
        System.out.printf("    Transaction ID: 0x%04x\n", this.transactionID);
        System.out.printf("    Flags: 0x%04x\n", this.flags);
        System.out.println("    Questions: " + this.numQuestions);
        System.out.println("    Answer RRs: " + this.numAnswerRRs);
        System.out.println("    Authority RRs: " + this.numAuthorityRRs);
        System.out.println("    Additional RRs: " + this.numAdditionalRRs);
        
        System.out.println("    Queries");
        System.out.printf("        %s: type %s, class IN\n", this.queryName, getTypeString(this.queryType));
        System.out.println("            Name: " + this.queryName);
        System.out.println("            [Name Length: " + this.queryNameLength + "]");
        System.out.println("            [Label Count: " + this.queryLabelCount + "]");
        System.out.println("            Type: " + getTypeString(this.queryType) + " (" + this.queryType + ")");
        System.out.printf("            Class: IN (0x%04x)\n", this.queryClass);
        
        System.out.println("    Answers");
        for (Answer answer : this.answers) {
            if (answer != null) {
                System.out.printf("        %s: type %s, class IN, addr %s\n", 
                                  answer.name, getTypeString(answer.type), answer.address);
                System.out.println("            Name: " + answer.name);
                System.out.println("            Type: " + getTypeString(answer.type) + " (" + answer.type + ")");
                System.out.printf("            Class: IN (0x%04x)\n", answer.dnsClass);
                System.out.println("            Time to live: " + answer.ttl + " (" + answer.ttl + " seconds)");
                System.out.println("            Data length: " + answer.dataLength);
                System.out.println("            Address: " + answer.address);
            }
        }
    }

    // Helper method to convert type code to string
    private String getTypeString(short type) {
        switch (type) {
            case (short)0x0001: return "A";
            case (short)0x0005: return "CNAME";
            case (short)0x0010: return "TXT";
            case (short)0x001c: return "AAAA";
            default: return "UNKNOWN";
        }
    }

    public byte[] getData(){
        return this.data;
    }
    public int getLength(){
        return this.data.length;
    }
    public String getTransactionID(){
        return String.format("%04x", this.transactionID);
    }

}
