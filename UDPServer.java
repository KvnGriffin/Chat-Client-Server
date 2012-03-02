/*UDPServer.java
 * Kevin Griffin.
 * Email: kevingriffin79@gmail.com
 * Server program that waits for initial client message to be received and then creates a thread for each
 * client and records each logged on client in an array list
 * To start server press Y and enter, to stop ctrl c.
 * */
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.BorderFactory;


public class UDPServer {
	public static final int serverport = 7778;   // servers port number
	public static ArrayList<ClientsConnected> clientsConnected = new ArrayList<ClientsConnected>(); // an array list of clients connected
	 public static DatagramSocket udpServerSocket;  // servers socket
	
	/****** Constructor calls method listenSocket() that listenss for incoming clients to connect **/
    UDPServer () {
		 listenSocket();  // call to listenSocket
		} 
       
       
    public void listenSocket() { 
		/* To start server get input from user */
		String input= null;
		System.out.println("Would you like to start the server? Y/N");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
	    input = reader.readLine();
		  } catch (IOException er) {
	              System.out.println(er);
             }
		
		if (input.equals("Y") || input.equals("y"))
            {
            while(true)  // Start to listen on servers socket
            {

				System.out.println("Server has started");  
				 byte[] receiveData = new byte[1024];
              // create an empty DatagramPacket packet
              DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
              byte[] reply = new byte[1024];
               
             try {
	         udpServerSocket.receive(receivePacket);  // receive incoming packets 
             } catch (IOException er) {
	              System.out.println(er);
             }
                if (receivePacket != null)
                {  
					int prt = receivePacket.getPort();
					String s = new String(receivePacket.getData(),0,receivePacket.getLength());
					/** If message is prepended with "HAL" server creates an instance of that client and adds them
					 * to the arrayList of connected Clients  */
					String substr = s.substring(0,3);
					String nameSubstr = s.substring(3,s.length());
					String serverResponce = "Thank you " + nameSubstr + " you have been authenticated by the server";
                    reply = serverResponce.getBytes();
					if(substr.equals("HAL"))
					{ 
						/****************** Reply from server ************/
						int replyPort = receivePacket.getPort();
						System.out.println("server replying on port: " + replyPort);
						DatagramPacket okPacket = new DatagramPacket(reply, reply.length, receivePacket.getAddress(), replyPort);
						try {
						udpServerSocket.send(okPacket);
						 } catch (IOException er) {
	                          System.out.println(er);
			               }
					// create new thread and client connected	
					  System.out.print("Creating a new Thread for: " + nameSubstr); 
					  System.out.println(", on port:" + prt); 			
			 	      clientsConnected.add(new ClientsConnected(receivePacket, nameSubstr));
			 	     ClientThread t = new ClientThread(udpServerSocket, this);
			 	      new Thread(t).start();
			 	    } // end of if
			 	 
				}
		         receiveData =new byte[1024];  // flush packet
			 }
		    } // end of while	
	    
    } // end of listen socket
     
    
   
	
    public static void main(String [] args) throws Exception {
       
		udpServerSocket= new DatagramSocket(serverport); // create the socket
	    UDPServer serv = new UDPServer(); // start the program running
	} // end of main

} // end class UDPServer


   

class ClientThread  implements Runnable {
	UDPServer server;  // an instance of the server to be passed into constructor
	DatagramSocket udpServerSocket;

	public ClientThread(DatagramSocket udpServerSocket, UDPServer server){
		this.server = server;
		this.udpServerSocket = udpServerSocket;
	} // end constructor
	
	/* sends the incoming message to all but who it was received from by checking the port numbers */
	 public void sendMessageToAll (DatagramPacket indata)
	{
	    DatagramPacket inPacket  = indata;
	    byte[] data = new byte[1024];
		String info = new String(indata.getData());
		int incomingPort = inPacket.getPort();  // the incoming port number
		data = info.getBytes();
		 
		System.out.println("info coming in: " + info);
      /* iterate through each client and send them the packet, except who sent it */ 
		for(int i=0; i<server.clientsConnected.size(); i++)
		{
			int prt = server.clientsConnected.get(i).getPortNumber(); //the other clients ports
			if(incomingPort != prt)
			{
			InetAddress ip = server.clientsConnected.get(i).getIPAddress();
			DatagramPacket output = new DatagramPacket(data, data.length,ip,prt);
			try {
				  udpServerSocket.send(output);
			 } catch (IOException er) {
	              System.out.println(er);
             } // end try
		    } // end if
		}
	}// end of sendMessageToAll
	
    
    
	/* override run to and send packet received to method sendMessageToAll */
	public void run(){
		
        while(true)
            {  
			 byte[] receiveData = new byte[1024];
              // create an empty DatagramPacket packet
              DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
             try {
	         udpServerSocket.receive(receivePacket);
             } catch (IOException er) {
	              System.out.println(er);
             }
                if (receivePacket != null)
                {  
					sendMessageToAll(receivePacket);
				 }  // end of if
			} // end of while		
		} // end of run()
	
} // end of class ClientThread


class ClientsConnected {
	private DatagramPacket receivePacket;
	int portNumber;
	String name;
	InetAddress receivePacketAddress;
	
	ClientsConnected(DatagramPacket dg, String name) {
	    this.receivePacket = dg;	
	    this.name = name;
	} // end constructor
	
	
	public InetAddress getIPAddress() {
		this.receivePacketAddress = receivePacket.getAddress();
		return receivePacketAddress;
	} // end of getter returns IPAddress
	
	public int getPortNumber() {
		this.portNumber = receivePacket.getPort();
		return portNumber;
	} // end of getter returns Port number
	
	
} // end of class
