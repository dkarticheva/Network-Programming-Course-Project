package com.fmi.np.pjt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.pfv.spmf.algorithms.frequentpatterns.apriori_inverse.AlgoAprioriInverse;;

public class ServerThread extends Thread {
	
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private double minsup;
	private double maxsup;
	private String fileName;
	private File outputFile;
	
	private HashMap<String,Integer> map;
	
	public ServerThread(Socket socket) {
		this.socket = socket;
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (IOException e) {
			System.out.println("Issue while obtaining input and output stream from socket");
		}
	}
	public void run() {
		receiveRequest();
		outputFile = new File("src/result.txt");
		String output = "src/result.txt";
		algorithm(minsup,maxsup,"src/test.txt",output);
		sendResponse();
	}
	private void algorithm(double msp,double mp,String it,String ot) {
		AlgoAprioriInverse aai = new AlgoAprioriInverse();
		try {
			aai.runAlgorithm(msp,mp,it,ot);
		} catch (IOException e) {
			System.out.println("Issue while using the algorithm");
		}
	}
	private void receiveRequest( ) {
		DataInputStream dis = new DataInputStream(is);
		BufferedInputStream bis = new BufferedInputStream(dis);
		File f = new File("tmp");
		
		try {
			long size = dis.readLong();
			minsup = dis.readDouble();
			maxsup = dis.readDouble();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
			byte[] buffer = new byte[4096];
			int nread;
			while (size > 0 && (nread = bis.read(buffer, 0, 4096)) > 0) {
				bos.write(buffer, 0, nread);
				size -= nread;
			}
			bos.flush();
			bos.close();
			map = prepareInputFile(f.getName());
			
		} catch (IOException e) {
			System.out.println("Issue while obtaining input and output stream from socket");
		}
		System.out.println("The request has been successfully received");
	}
	
	private HashMap<String,Integer> prepareInputFile(String fileName) {
		FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fileInputStream == null) {
        	return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
        String line = null;
        HashMap<String, Integer> hm = new HashMap<>();
        int count = 1;
        ArrayList<String> al = new ArrayList<>();
        try {
            line = br.readLine();
            line = "";
            while ((line = br.readLine())!=null) {
                String[] words = line.split(",(?! )");
                ArrayList<String> arrayList = new ArrayList<>();
                try {
                    arrayList.add(words[4]);
                    arrayList.add(words[6]);
                } catch (ArrayIndexOutOfBoundsException ignored) {}
                for (String s : arrayList) {
                    if(!hm.containsKey(s)){
                        hm.put(s,count);
                        count++;
                    }
                }
                for(String s: arrayList) al.add(s);
                al.add("\\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("src/test.txt"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        for (String str : al) {
        	if (str.equals("\\n")) {
        		try {
					bw.newLine();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	else {
        		try {
					 bw.write(hm.get(str).toString());
					 bw.write(" ");
					 bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	} 
        }
        return hm;
	}
	
	private void reverseNumbersToStrings() {
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(outputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter("src/finalResults.txt"));
			
			HashMap<Integer, String> reverseMap = new HashMap<>();

	        for (Map.Entry<String, Integer> res: map.entrySet()){
	        	reverseMap.put(res.getValue(),res.getKey());
	        }
	        
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(" ");
				if (words.length == 3) {
					Integer f = Integer.parseInt(words[0]);
					if (reverseMap.containsKey(f)) {
						 bw.write(reverseMap.get(f) + " ");
					}
					bw.write("#SUP: ");
					bw.write(words[2].toString());
					bw.newLine();
					bw.flush();
				}
				else if (words.length == 4) {
					Integer f = Integer.parseInt(words[0]);
					Integer s = Integer.parseInt(words[1]);
					bw.write(reverseMap.get(f) + " ");
					bw.write(reverseMap.get(s) + " ");
					bw.write("#SUP: ");
					bw.write(words[3].toString());
					bw.newLine();
					bw.flush();
				}
				
			}
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendResponse() {
		reverseNumbersToStrings();
		
		File finFile = new File("src/finalResults.txt");
		byte[] buf = new byte[4096];
		int nread = -1;
		try {
			BufferedInputStream br = new BufferedInputStream(new FileInputStream(finFile));
			BufferedOutputStream bos = new BufferedOutputStream(os);
			while ((nread = br.read(buf, 0, 4096)) > 0) {
				os.write(buf, 0, nread);
			}
			if (br!=null) br.close();
			finFile.delete();
			bos.close();
			
		} catch (IOException e) {
				System.out.println("Issue while sending the response");
		} finally {
			close();
		}
		System.out.println("The response has been sent to the client");
	}
	
	private void close() {
		try {
			socket.close();
		} catch(IOException e) {
			System.out.println("Error while closing socket!");
		}
	}

}
