/* **********************************************************************************
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░███╗░░██╗░█████╗░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░████╗░██║██╔══██╗░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██╔██╗██║██║░░██║░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██║╚████║██║░░██║░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██║░╚███║╚█████╔╝░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░╚═╝░░╚══╝░╚════╝░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
░░░░░░░░░░███╗░░░███╗░█████╗░██████╗░██╗███████╗██╗░█████╗░░█████╗░██████╗░░░░░░░░░░░
░░░░░░░░░░████╗░████║██╔══██╗██╔══██╗██║██╔════╝██║██╔══██╗██╔══██╗██╔══██╗░░░░░░░░░░
░░░░░░░░░░██╔████╔██║██║░░██║██║░░██║██║█████╗░░██║██║░░╚═╝███████║██████╔╝░░░░░░░░░░
░░░░░░░░░░██║╚██╔╝██║██║░░██║██║░░██║██║██╔══╝░░██║██║░░██╗██╔══██║██╔══██╗░░░░░░░░░░
░░░░░░░░░░██║░╚═╝░██║╚█████╔╝██████╔╝██║██║░░░░░██║╚█████╔╝██║░░██║██║░░██║░░░░░░░░░░
░░░░░░░░░░╚═╝░░░░░╚═╝░╚════╝░╚═════╝░╚═╝╚═╝░░░░░╚═╝░╚════╝░╚═╝░░╚═╝╚═╝░░╚═╝░░░░░░░░░░
 * **********************************************************************************
 */
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import static java.lang.System.out;

public class Server {
	private ServerSocket server;
	private Integer nThreads, portServer, delay;
	private ThreadPoolExecutor pool;

	private Server (Integer nThreads, Integer portServer, Integer delay){
		this.nThreads =     (Optional.ofNullable(nThreads).orElse(-1) == -1)?    2 : nThreads;
		this.portServer = (Optional.ofNullable(portServer).orElse(-1) == -1)? 1000 : portServer;
		this.delay = 	      (Optional.ofNullable(delay).orElse(-1) == -1)?    5 : delay;
	}// Server
	
	private void start() throws Exception {
		out.println("\n### Server was started on port " + portServer + " ###");
		out.println("### Threadpool started with " + nThreads + " threads ###");
		out.println("### Thread termination delay is " + delay + " seconds ###\n");
		server = new ServerSocket(portServer);
		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
		for (int i = 0; i < nThreads; i++) {
			ThreadServer threadServer = new ThreadServer(i, server, delay);
			pool.execute(threadServer);
		}
		pool.shutdown();
	}// start
	
	public static void main(String[] args) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0; i < args.length; i=i+2) {
			try{
				map.put( args[i].trim(), Integer.valueOf(args[i+1].trim()) );
			} catch(Exception e){
				map.put(args[i].trim(), -1);
				if( args[i].trim().equals("-help") ) i--;
			}
        }
		if ( map.containsKey("-help") ){
			out.println("Usage: java Server [options...]");
			out.println(" -threads <int> Defines the NUMBER of threads that Threadpool will use.");
			out.println(" -port <int>    Define the PORT on which the server will be waiting for the client.");
			out.println(" -delay <int>   Defines the waiting time (seconds) before reuse the thread.");
			out.println(" -help          Get help.");
		} else {
			Server newServer = new Server( map.get("-threads"), map.get("-port"), map.get("-delay") );
			try {
				newServer.start();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}// main
}