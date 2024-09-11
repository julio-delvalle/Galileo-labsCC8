public class DNSRequest {

    private byte[] data;
    // DNS Request Header
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
    private String[] queryNameParts;

    private short queryType;
    private short queryClass;


    // DNSRequest Constructor
    public DNSRequest(byte[] data, int dataLength, short transactionID, short flags, short numQuestions, short numAnswerRRs,
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
        // System.out.println("SE VA A CREAR DNSREQUEST CON DATA.length = "+dataLength);
        this.data = new byte[dataLength];
        System.arraycopy(data, 0, this.data, 0, dataLength);
    }



    // Method to set DNS request header information
    public void setDNSRequestHeader(short transactionID, short flags, short numQuestions, 
                                     short numAnswerRRs, short numAuthorityRRs, short numAdditionalRRs) {
        this.transactionID = transactionID;
        this.flags = flags;
        this.numQuestions = numQuestions;
        this.numAnswerRRs = numAnswerRRs;
        this.numAuthorityRRs = numAuthorityRRs;
        this.numAdditionalRRs = numAdditionalRRs;
        
    }


    // Print DNSRequest as Wireshark format
    public void printDNSRequest() {
        System.out.println("DNS REQUEST: ===========");
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
        
    }

    // Helper method to convert type code to string
    private String getTypeString(short type) {
        switch (type) {
            case 0x01: return "A";
            case 0x05: return "CNAME";
            case 0x10: return "TXT";
            case 0x1c: return "AAAA";
            default: return "UNKNOWN";
        }
    }


    public void setRecursionDesired(){
        this.flags = (short) (this.flags | 0x0100); // Set the RD (Recursion Desired) bit to 1

        this.data[2] = (byte) (this.data[2] | 0x01); // Set the RD (Recursion Desired) bit to 1
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
