public class DNSResponse {
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
    private int queryLabelCount;
    private short queryType;
    private short queryClass;

    // Answer information
    private Answer[] answers;

    public static class Answer {
        private String name;
        private short type;
        private short dnsClass;
        private int ttl;
        private short dataLength;
        private String address;

        // Constructor
        public Answer(String name, short type, short dnsClass, int ttl, short dataLength, String address) {
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
    public DNSResponse(short transactionID, short flags, short numQuestions, short numAnswerRRs,
                       short numAuthorityRRs, short numAdditionalRRs, String queryName,
                       int queryNameLength, int queryLabelCount, short queryType, short queryClass) {
        this.transactionID = transactionID;
        this.flags = flags;
        this.numQuestions = numQuestions;
        this.numAnswerRRs = numAnswerRRs;
        this.numAuthorityRRs = numAuthorityRRs;
        this.numAdditionalRRs = numAdditionalRRs;
        this.queryName = queryName;
        this.queryNameLength = queryNameLength;
        this.queryLabelCount = queryLabelCount;
        this.queryType = queryType;
        this.queryClass = queryClass;
        this.answers = new Answer[numAnswerRRs];
    }

    // Method to add an answer
    public void addAnswer(int index, String name, short type, short dnsClass, int ttl, short dataLength, String address) {
        if (index < numAnswerRRs) {
            answers[index] = new Answer(name, type, dnsClass, ttl, dataLength, address);
        }
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
        this.answers = new Answer[numAnswerRRs];
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
        System.out.println("            Type: " + getTypeString(this.queryType) + " (" + this.queryType + ") (Host Address)");
        System.out.printf("            Class: IN (0x%04x)\n", this.queryClass);
        
        System.out.println("    Answers");
        for (Answer answer : this.answers) {
            if (answer != null) {
                System.out.printf("        %s: type %s, class IN, addr %s\n", 
                                  answer.name, getTypeString(answer.type), answer.address);
                System.out.println("            Name: " + answer.name);
                System.out.println("            Type: " + getTypeString(answer.type) + " (" + answer.type + ") (Host Address)");
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
            case 1: return "A";
            case 5: return "CNAME";
            case 16: return "TXT";
            case 28: return "AAAA";
            default: return "UNKNOWN";
        }
    }

}
