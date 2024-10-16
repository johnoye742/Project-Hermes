package com.johnoye742.hermes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BenchMarking {
    private static final String HOST = "localhost";
    private static final int PORT = 2907;
    private static final int NUM_REQUESTS = 100000;
    private static final int NUM_CLIENTS = 100;
    private static final int PIPELINE = 16;

    public static void main(String[] args) {
    	StringBuilder sb = new StringBuilder("");
    	sb.append("{\n");
        // Benchmark SET requests
        double setRequestsPerSecond = benchmarkSetRequests();
        System.out.printf("SET: %.2f requests per second%n", setRequestsPerSecond);

        sb.append("\t\"SET\": \"").append(setRequestsPerSecond).append(" requests per second\", \n");
        // Benchmark GET requests
        double getRequestsPerSecond = benchmarkGetRequests();
        System.out.printf("GET: %.2f requests per second%n", getRequestsPerSecond);
        sb.append("\t\"GET\": \"").append(getRequestsPerSecond).append(" requests per second\" \n");
        
        sb.append("},\n");
        
        File benchMarkLog = new File(System.getProperty("user.home") + File.separator + "hermes-benchmark-results.json");
        
        try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(benchMarkLog, true));
			writer.append(sb.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private static double benchmarkSetRequests() {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            executor.submit(() -> {
                try {
                    Socket socket = new Socket(HOST, PORT);
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                    for (int j = 0; j < NUM_REQUESTS / NUM_CLIENTS; j += PIPELINE) {
                        for (int k = 0; k < PIPELINE; k++) {
                            int requestNumber = j + k + 1;
                            String key = "key" + requestNumber;
                            String value = "value" + requestNumber;
                            String setRequest = "SET " + key + " " + value;
                            out.println(setRequest);
                        }
                    }

                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Shut down executor and wait for tasks to finish
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;

        return (NUM_REQUESTS * 1000.0) / durationMillis;
    }

    private static double benchmarkGetRequests() {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            executor.submit(() -> {
                try {
                    Socket socket = new Socket(HOST, PORT);
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                    for (int j = 0; j < NUM_REQUESTS / NUM_CLIENTS; j += PIPELINE) {
                        for (int k = 0; k < PIPELINE; k++) {
                            int requestNumber = j + k + 1;
                            String key = "key" + requestNumber;
                            String getRequest = "GET " + key;
                            out.println(getRequest);
                        }
                    }

                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Shut down executor and wait for tasks to finish
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;

        return (NUM_REQUESTS * 1000.0) / durationMillis;
    }
}
