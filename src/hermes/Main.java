package hermes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final Map<String, Object> database = new ConcurrentHashMap<>(100, 5, 1000000);
    private static final StringBuilder logs = new StringBuilder();
    private static final String rootDir = System.getProperty("user.home") + "/Project-Hermes";
    private static final String logFileAddr = rootDir + File.separator + "logs" + File.separator + "req_log.herm";
    private static final int THREAD_POOL_SIZE = 100;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        try (ServerSocket ss = new ServerSocket(2907)) {
        	
            System.out.println("-- Hermes Initiated --");
            Thread bootLogThread = new Thread(new Runnable() {
            	@Override
            	public void run() {
            		bootFromLog();
					System.out.println("Finished booting from log");
            	}
            });
            
            bootLogThread.start();
			
            while (true) {
                Socket s = ss.accept();
                
                threadPool.execute(() -> handleClient(s));
            }
        } catch (IOException e) {
            System.err.println("Could not start server");
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            saveLog();
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    System.out.println(line);
                    
                    if(!line.startsWith("exit")) logs.append(line).append("\n");
                    out.println(handleRequest(line));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String get(String key) {
        return database.containsKey(key) ? (String) database.get(key) : "Could not find data with key: " + key;
    }

    private static void set(String key, String[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < values.length; i++) {
            sb.append(values[i]).append(" ");
        }
        database.put(key, sb.toString().stripTrailing());
    }
    
    private static void arrayPush(String key, String value) {
    	if(database.containsKey(key)) {
    		ArrayList<String> array = (ArrayList<String>) database.get(key);
    		array.add(value);
    		return;
    	} else {
    		ArrayList<String> array = new ArrayList<>();
    		array.add(value);
    		database.put(key, array);
    	}
    }
    
    private static String arrayGet(String key) {
    	return (String) database.get(key).toString();
    }

    private static void saveLog() {
        File logDir = new File(rootDir + File.separator + "logs");
        File logFile = new File(logFileAddr);
        logDir.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.append(logs.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void bootFromLog() {
        File logFile = new File(logFileAddr);
        if (logFile.exists()) {
            try (Scanner sc = new Scanner(logFile).useDelimiter("\n")) {
                while (sc.hasNext()) {
                    handleRequest(sc.nextLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getAll() {
        return database.toString();
    }

    private static void add(String key, String value) {
        try {
            int addition = Integer.parseInt(value);
            int val = Integer.parseInt(get(key));
            database.put(key, String.valueOf(val + addition));
        } catch (NumberFormatException e) {
            database.put(key, get(key) + value);
        }
    }

    private static String concat(String key, String[] values) {
        StringBuilder sb = new StringBuilder(get(key));
        for (int i = 2; i < values.length; i++) {
            sb.append(values[i]).append(" ");
        }
        database.put(key, sb.toString().stripTrailing());
        return "Successfully concatenated to string: " + key;
    }

    private static String handleRequest(String request) {
        try {
            String[] formatted = request.split(" ");
            switch (formatted[0].toLowerCase()) {
                case "get":
                    return get(formatted[1]);
                case "set":
                    set(formatted[1], formatted);
                    return "Set successfully";
                case "exit":
                    saveLog();
                    System.exit(0);
                    break;
                case "all":
                    return getAll();
                case "add":
                    add(formatted[1], formatted[2]);
                    return "Added successfully";
                case "concat":
                    return concat(formatted[1], formatted);
                case "array_push":
                	arrayPush(formatted[1], formatted[2]);
                	return "Pushed array successfully";
                case "array_get":
                	return arrayGet(formatted[1]);
                default:
                    return "Invalid command";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Wrong number of arguments";
        }
        return "Unhandled request";
    }
}
