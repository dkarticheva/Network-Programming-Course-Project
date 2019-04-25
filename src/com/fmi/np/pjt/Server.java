package com.fmi.np.pjt;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	final static int DEFAULT_PORT = 4444;
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			
			serverSocket = new ServerSocket(DEFAULT_PORT);
			System.out.println("Server is running");
			
			while (true) {
				Socket socket = serverSocket.accept();
				ServerThread thread = new ServerThread(socket);
				thread.start();
			}
			
		} catch (IOException e) {
			System.out.println("Issue while opening the ServerSocket");
		}
		if(serverSocket!=null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Issue while closeing serverSocket");
			}
		}
	}

}
