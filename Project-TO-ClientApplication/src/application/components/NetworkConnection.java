package application.components;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class NetworkConnection {

	private static ObjectInputStream objIn = null;
	private static ObjectOutputStream objOut = null;
	private static Socket s = null;
	
	private static int refreshPeriodInMS = 2000;

	public static void sendSocketEvent(SocketEvent se) {
		try {
			if (se != null) {
				objOut.writeObject(se);
			}
			else System.out.println("SocketEvent is null");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static SocketEvent rcvSocketEvent() {
		SocketEvent se = null;
		try {
			se = (SocketEvent) objIn.readObject();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return se;
	}

	public static void connect(String address, int port) {
		try {
			s = new Socket(address, port);
			objOut = new ObjectOutputStream(s.getOutputStream());
			objIn = new ObjectInputStream(s.getInputStream());
			
			System.out.println("    Connected to server.");
		}
		catch (ConnectException e) {
			System.out.println("    Server is not responding. Next try in " + refreshPeriodInMS/1000 + " seconds.");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			NetworkConnection.connect(address, port);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void disconnect() {
		try {
			objIn.close();
			objOut.close();
			s.close();
			// objIn and objOut are closed
			objOut = null;
			objIn = null;
			s = null;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
