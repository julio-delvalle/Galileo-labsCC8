import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.*;


public class DNSServer {
    private DatagramSocket socket;
    private Integer nThreads, portServer, delay;
    private ThreadPoolExecutor pool;
    static Logger LOGGER = Logger.getLogger("Server");

    private DNSServer(Integer nThreads, Integer portServer, Integer delay) {
        this.nThreads = nThreads;
        this.portServer = portServer;
        this.delay = delay;
    }

    private void start() throws IOException {
        socket = new DatagramSocket(portServer);
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            ThreadServer threadServer = new ThreadServer(i, socket, delay);
            pool.execute(threadServer);
        }
        pool.shutdown();
    }

    public static void main(String[] args) throws SocketException {
        DNSServer newServer = new DNSServer( 1, 53, 3000 );
        try {
            newServer.start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}