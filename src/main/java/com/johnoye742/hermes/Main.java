package com.johnoye742.hermes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import com.google.gson.*;

public class Main {

    private static Map<String, Object> database = new ConcurrentHashMap<>(100, 5, 1000000);
    private static final StringBuilder logs = new StringBuilder();
    private static final String rootDir = System.getProperty("user.home") + "/Project-Hermes";
    private static final String logFileAddr = rootDir + File.separator + "logs" + File.separator + "req_log.herm";
    private static final String stateFile = rootDir + File.separator + "state" + File.separator + "state.ser";

    public static void main(String[] args) {
        try (ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor()) {
            try (ServerSocket ss = new ServerSocket(2907)) {

                System.out.println("-- Hermes Initiated --");
                Thread bootLogThread = new Thread(() -> {
                    resumeState();
                    System.out.println("Finished booting from log");
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
                saveState();
                saveLog();

            }
        }

    }
    private static void resumeState() {
        try {
            // sf = state file directory
            File sf = new File(rootDir + File.separator + "state");
            File state  = new File(stateFile);

            if(!sf.exists()) {
                // Checks if the state file directory was created
                if(sf.mkdirs()) {
                    // Checks if the state file exists already
                    if(!state.exists()) {
                        // Create a new file and if successful output a message
                        if(state.createNewFile()) System.out.println("Created new state file.");
                    }
                }
                // do not continue if the directory didn't exist
                return;
            }
            // If the directory exist, try to read from the file
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(stateFile));
            // Read the object from the Object stream
            Object obj = ois.readObject();
            // Try to cast the object to a Map and assign it to the `database` variable
            if(obj	!= null) database = (Map<String, Object>) obj;

            // Close the ObjectInputStream when we're done
            ois.close();

        } catch (Exception e) {
            // If there were issues, log it
            System.out.println("There might have being issues booting from persistent storage");
        }

    }

    private static void saveState() {
        try {
            FileOutputStream fos = new FileOutputStream(stateFile);
            ObjectOutputStream stream = new ObjectOutputStream(fos);

            stream.writeObject(database);

            stream.flush();
            stream.close();
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Couldn't write the persistent data");
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

    private static void arrayPush(String key, String[] values) {
        if(database.containsKey(key)) {
            ArrayList<String> array = (ArrayList<String>) database.get(key);
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < values.length; i++) {
                sb.append(values[i]).append(" ");
            }

            array.add(sb.toString().stripTrailing());
            return;
        } else {
            ArrayList<String> array = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < values.length; i++) {
                sb.append(values[i]).append(" ");
            }
            array.add(sb.toString().stripTrailing());
            database.put(key, array);
        }
    }

    private static String arrayGet(String key) {
        Gson json = new Gson();

        return json.toJson(database.get(key));
    }

    private static void saveLog() {
        File logDir = new File(rootDir + File.separator + "logs");
        File logFile = new File(logFileAddr);
        logDir.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.append(logs.toString());
            writer.close();
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
        return new Gson().toJson(database);
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

    /**
        {@code @method} concat
        @return String
     */
    private static String concat(String key, String[] values) {
        StringBuilder sb = new StringBuilder(get(key));
        for (int i = 2; i < values.length; i++) {
            sb.append(values[i]).append(" ");
        }
        database.put(key, sb.toString().stripTrailing());
        return "Successfully concatenated to string: " + key;
    }

    private static void delete(String key) {
        database.remove(key);
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
                    saveState();
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
                    arrayPush(formatted[1], formatted);
                    return "Pushed array successfully";
                case "array_get":
                    return arrayGet(formatted[1]);
                case "delete":
                    delete(formatted[1]);
                    return "Deleted the item successfully";
                default:
                    return "Invalid command";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Wrong number of arguments";
        }
        return "Unhandled request";
    }
}