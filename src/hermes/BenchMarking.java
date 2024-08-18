package hermes;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchMarking {
	public static int requestsCompleted = 0;
    public static void main(String[] args) {
        String host = "localhost";
        int port = 2907;
        int numRequests = 100; // You can adjust the number of requests as needed
        ExecutorService exec = Executors.newFixedThreadPool(20);
        
        try {
            long startTime = System.currentTimeMillis();
            
exec.execute(new Runnable() {
            		@Override
            		public void run() {
            			for (int i = 1; i <= numRequests; i++) {
            	
            			try {
							// Create a new socket connection to the server
							Socket socket = new Socket(host, port);

							// Prepare the request
							String key = "key" + i;
							String value = "value" + i;
							String request = "SET " + key + " " + value;

							// Send the request
							PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
							out.println(request);

							// Close the socket immediately after sending the request
							socket.close();

							// Increment the number of completed requests
							requestsCompleted++;
							System.out.println(requestsCompleted);
            			} catch (Exception e) {
            				
            			}
            		}
            		}
            	});
            
			while(exec.awaitTermination(1, null)) {
				exec.shutdown();
			}
            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;
            System.out.println(durationMillis);

            double requestsPerSecond = (requestsCompleted * 1000.0) / durationMillis;

            System.out.printf("SET: %.2f requests per second%n", requestsPerSecond);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
