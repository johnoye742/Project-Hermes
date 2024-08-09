package hermes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {
	
	public static Map<String, Object> database;
	public static StringBuilder logs;
	public static String rootDir = System.getProperty("user.home") + "/Project-Hermes";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// Initialize variables
			database = new HashMap<String, Object>();
			logs = new StringBuilder("");
			// Initiate server
			ServerSocket ss = new ServerSocket(2907);
			
			// Welcome message
			System.out.println("-- Hermes Initiated --"); 
			
			while(true) {
				// Accept incoming requests
				Socket s = ss.accept();
			
				// Read the input stream for data from the request
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
				String line = br.readLine();
				
				
				// Print the request
				System.out.println(line);
				
				if(!line.toLowerCase().startsWith("exit")) logs.append(line + "\n");
				
				
				// Send a response to the client
				PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
				out.println(handleRequest(line));
				
			}
			
		} catch (IOException e) {
			System.err.println("Could not start server");
		}
	}
	
	public static String get(String key) {
		if(database.containsKey(key)) {
			return (String) database.get(key);
		}
		
		return "Could not find data with key: "+key;
	}
	
	public static void set(String key, String[] values) {
		StringBuilder sb = new StringBuilder("");
		
		for(int i = 2; i < values.length; i++) {
			sb.append(values[i] + " ");
		}
		database.put(key, sb.toString().stripTrailing());
	}
	
	public static void saveLog() {
		try {
			File logDir = new File(rootDir + File.separator + "logs");
			File logFile = new File(rootDir + File.separator + "logs" + File.separator + "req_log.herm");
			logDir.mkdirs();
			
			if(!logFile.exists()) logFile.createNewFile();
			
			FileWriter fw = new FileWriter(logFile);
			fw.append(logs.toString());
			
			fw.flush();
			fw.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getAll() {
		StringBuilder sb = new StringBuilder("");
		sb.append("{");
		
		for(String key : database.keySet()) {
			sb.append(key + ": " + '"' + database.get(key) + '"' + ", ");
			
		}
		
		sb.delete(sb.length()-2, sb.length());
		
		sb.append("}");
		
		
		return sb.toString();
	}
	
	public static String handleRequest(String request) {
		String[] formatted = request.split(" ");
		
		// Using a switch-case for better performance
		switch(formatted[0].toLowerCase()) {
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
		}
		
		return "Seems there was a problem with your request";
	}

}
