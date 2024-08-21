package hermes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class TestApp {

	public static void main(String[] args) {
        String hostname = "localhost";
        int port = 2907;
        System.out.print("Enter a request: ");
        Scanner sc = new Scanner(System.in);
        String request = sc.nextLine();
        
        if(request.toLowerCase() == "q") System.exit(0);

        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
            
            // Send a message to the server
            out.println(request);
            
            if(request.toLowerCase() == "exit") System.exit(0);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 
			System.out.println(br.readLine());
            
            main(args);
            
        } catch (IOException e) {
            System.err.println("Client error");
            e.printStackTrace();
        }
    }

}
