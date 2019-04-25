package com.fmi.np.pjt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private Socket socket;

	public Client(InetAddress address, int port) {
		try {

			socket = new Socket(address, port);
			System.out.println("Successfully connected to server");

		} catch (IOException e) {
			System.out.println("Issue while opening socket on address " + address + " and on port" + port);
		}
	}

	public void retrieveInput() {
		boolean fileEntered = false;
		boolean minEntered = false;
		boolean maxEntered = false;
		File file;
		double minsup;
		double maxsup;
		Scanner sc1, sc2, sc3;
		String filename = null;
		do {
			System.out.println("Please enter a valid input file for the AprioriInverse Algorithm");
			sc1 = new Scanner(System.in);
			filename = sc1.nextLine();
			file = new File(filename);
			if (file.exists()) {
				fileEntered = true;
			}
		} while (!fileEntered);

		do {
			System.out.println("Please enter a valid value for the minimum "
					+ "support for the AprioriInverse Algorithm (should be a double between 0.0 and 1.0)");
			sc2 = new Scanner(System.in);
			minsup = sc2.nextDouble();
			if (minsup >= 0.0 && minsup <= 1.0) {
				minEntered = true;
			}
		} while (!minEntered);

		do {
			System.out.println("Please enter a valid value for the maximum "
					+ "support for the AprioriInverse Algorithm (should be a double between 0.0 and 1.0)");
			sc3 = new Scanner(System.in);
			maxsup = sc3.nextDouble();
			if (maxsup >= 0.0 && maxsup <= 1.0) {
				maxEntered = true;
			}
		} while (!maxEntered);

		sc1.close();
		sc2.close();
		sc3.close();
		sendInput(filename, minsup, maxsup);
		receiveOutput(filename);
		close();
	}

	private void close() {
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Error closing socket!");
		}
	}

	private void sendInput(String filename, double minsup, double maxsup) {

		try {
			OutputStream os = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			BufferedOutputStream bos = new BufferedOutputStream(dos);
			File f = new File(filename);
			dos.writeLong(f.length());
			dos.writeDouble(minsup);
			dos.writeDouble(maxsup);
			dos.flush();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			byte[] buf = new byte[4096];
			int nread = -1;
			while ((nread = bis.read(buf, 0, 4096)) != -1) {
				bos.write(buf, 0, nread);
			}

			bos.flush();
			bis.close();

		} catch (IOException e) {
			System.out.println("Issue while accessing the output stream of the socket");
		}
		System.out.println("The request has been successfully sent to the server");
	}

	private void receiveOutput(String filename) {
		try {
			InputStream is = socket.getInputStream();
			BufferedInputStream br = new BufferedInputStream(is);

			File f = new File("client_result.txt");
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));

			byte[] buf = new byte[4096];
			int nread = -1;
			while ((nread = br.read(buf, 0, 4096)) > 0) {
				bos.write(buf, 0, nread);
			}
			bos.flush();
			bos.close();
			System.out.println("Result file received!");
			socket.close();

		} catch (IOException e) {
			System.out.println("Issue while receiving the response from the server");
		}

	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage Client <hostname> <port (default: 11087)>");
			return;
		}

		InetAddress adr;
		int port;
		try {
			adr = InetAddress.getByName(args[0]);
			port = Integer.parseInt(args[1]);
			Client c = new Client(adr, port);
			c.retrieveInput();
		} catch (UnknownHostException e) {
			System.out.println("The entered host is unknown");
		}
	}
}
