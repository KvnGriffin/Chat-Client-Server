/* UDPClient.java
 * Kevin Griffin
 * Email: kevingriffin79@gmail.com
 * UDP chat client that uses a GUI to log on to server. Client then listens constantly for incoming
 * packets and displays them in the messageArea of GUI. 
 * Messages are sent via the textField, once enter has been pressed.
 * */


import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.BorderFactory;

public class UDPClient implements Runnable {
	
	public final static int SERVERPORT = 7778;  // the servers port number
	static DatagramSocket udpClientSocket = null;  // socket to handle connection
	static InetAddress serverIPAddress;   // address of machine(localhost passed in later)
    String name;   // the user''s name
    
    public void setName(String nm) {   // sets client name
	    this.name = nm;
	} // end of setter 
	
    public String getName() {  // gets clients name
	    return name;	
	} // end of getter
    
    String text = "Welcome to UDP Chat.... \nPlease choose Log On from the file menu above and enter \n your screen name.";
    JFrame frame = new JFrame("              UDP Chat");  // frame to hold each panel
    JPanel panel1 = new JPanel(new BorderLayout()); 
    JPanel panel2 = new JPanel();  
    JMenuBar menuBar = new JMenuBar();
    JTextField textField = new JTextField(30);  // text field to submit chat
    JTextArea messageArea = new JTextArea(text,20,25);  // message area to display chat
  
    public JMenu createFileMenu() {  // creates the file menu for use in GUI
		JMenu menu = new JMenu("File");
		menu.add(createLogOnItem());  // add LogOn filed
		menu.add(createFileExitItem()); // add exit field
		return menu;
		} // end createFileMenu()
	
	public JMenuItem createLogOnItem() {
		JMenuItem item = new JMenuItem("Log On");
		class MenuItemListener implements ActionListener {
			public void actionPerformed (ActionEvent event) {
				// show input dialog below sets the info on the pop up window
			    String nme = JOptionPane.showInputDialog(null,"Please enter your name:","Log On",JOptionPane.INFORMATION_MESSAGE);
				setName(nme);
				/** Sending packet to server first to recieve an ok message and create an instance 
				 * of the client in the server */
    
      // get the IP address of the local machine - we will use this as the address to send the data to (i.e. the server program running locally)
     try {       
     serverIPAddress = InetAddress.getByName("localhost");
         }catch (IOException er) {
					System.out.println(er);
		              }   
    
      // create byte buffer to hold the message to send, which is at lease as long as the number of bytes in the message
      byte[] sendData = new byte[1024]; 

      // form a message to send
      String clientRequest = "HAL"+name; // prepend HAL to the start of first name so server knows client is trying to log on
      
      // put this message into our emty buffer/array of bytes
      sendData = clientRequest.getBytes();
      
      // create a DatagramPacket with the data, IP address and port number
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, SERVERPORT);
      
          // send the UDP packet
      try {
		  messageArea.append("Packet sent to Server \n"); // Display sending on screen	
              udpClientSocket.send(sendPacket);   // send the packet
           } catch (IOException er) {
					System.out.println(er);
		              } 
      
			
			} // end actionPerformed
		} // end MenuItemListener
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener); // add listener to the menu 'item'
		return item;
	} // end createLogOnItem
	
	public JMenuItem createFileExitItem() {     // to close the program
	    JMenuItem item = new JMenuItem("Exit");
	    class MenuItemListener implements ActionListener {
			public void actionPerformed (ActionEvent event) {
			    System.exit(0);       // close the program by calling System.exit(0)
			} // end actionPerformed
		} // end MenuItemListener
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
	    return item;
	}	
  
	UDPClient() {
       /**** Layout of GUI *****/
		frame.setJMenuBar(menuBar);
        menuBar.add(createFileMenu());
      
		textField.setEditable(true);
		messageArea.setEditable(false);
		frame.setSize(500,700);
        
        panel1.add(messageArea);
        panel1.add(new JScrollPane(messageArea),"Center");
        panel2.add(new JLabel("Message:"));
        panel2.add(textField);   
        // specify the borders
        panel1.setBorder(BorderFactory.createLineBorder(Color.lightGray, 15));
        panel2.setBorder(BorderFactory.createLineBorder(Color.lightGray, 10));  
        // add panels to frame	   
		frame.getContentPane().add(panel1, BorderLayout.CENTER);
		frame.getContentPane().add(panel2,BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		// Listeners for the textField packet is sent when enter is pressed.
		 textField.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
			byte[] data = new byte[1024];
			String info = getName() + ": " + textField.getText(); // specifies format to be sent
			data = info.getBytes();
			DatagramPacket output = new DatagramPacket(data, data.length,serverIPAddress,SERVERPORT);
			try {
			udpClientSocket.send(output);
				} catch(IOException er) {
				 System.out.println(er);
			 } // end of try
			data = new byte[1024]; // flush byte array data
		    textField.setText("");  // reset textfiled to blank
		}
	    });    // end of addActionListener
	
	} // end of constructor
	
	public void run() {   // overriding run() method, listens for incoming messages and adds to messageArea
	
	    byte[] info = new byte[1024];
	    String s = "";
	    while(true)
	    {
			DatagramPacket dp = new DatagramPacket(info, info.length);
			try {
				 udpClientSocket.receive(dp);
		         s = new String (dp.getData());
			     messageArea.append(s + "\n");   // add to message Area
				} catch(IOException er) {
				 System.out.println(er);
			 } // end of try
			 
		} // end of while
		 
	}
     
    
	
	public static void main(String [] args) {	
	
       // create a DatagramSocket
        try {
            udpClientSocket = new DatagramSocket();
        }catch (IOException er) {
					System.out.println(er);
		             } 
        
        Thread t = new Thread(new UDPClient()); // starts the cliebt on a new thread listening for incoming packets
		t.start();  // start thread

    } // end of main	
	
} // end of class UDPClient	

	



