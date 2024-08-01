package hermes;

import java.io.BufferedReader;
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			database = new HashMap<String, Object>();
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
			case "all":
				 return getAll();
		}
		
		return "Seems there was a problem with your request";
	}

}
