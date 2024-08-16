package hermes;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class BenchMarking {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 6379;
        int numRequests = 100; // You can adjust the number of requests as needed

        try {
            long startTime = System.currentTimeMillis();
            int requestsCompleted = 0;

            for (int i = 1; i <= numRequests; i++) {
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
            }

            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;

            double requestsPerSecond = (requestsCompleted * 1000.0) / durationMillis;

            System.out.printf("SET: %.2f requests per second%n", requestsPerSecond);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
