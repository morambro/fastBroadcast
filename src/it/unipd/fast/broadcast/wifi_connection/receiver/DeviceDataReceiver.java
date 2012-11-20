package it.unipd.fast.broadcast.wifi_connection.receiver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;


/**
 * Thread used to receive data from other connected devices. It creates a Socket and waits for incoming connections; 
 * once the message has been received the socket is closed, and the DataReceiver waits for a new incoming one 
 * until stopDataReceiver() method is called
 * 
 * @author Moreno Ambrosin
 *
 */
public class DeviceDataReceiver extends Thread{
	protected final String TAG = "it.unipd.fast.broadcast";

	/**
	 * UDP expected packet size, 1KByte
	 */
	public static final int PACKET_SIZE = 1024;


	private boolean terminated = false;
	private ServerSocket server_socket;
	private DataCollectionHandler handler;

	/**
	 * Interface used to specify operation to do on data collected or when an error occurs
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public static interface DataCollectionHandler{
		
		/**
		 * Called when data are collected
		 * 
		 * @param result
		 * @param sender
		 */
		public void onDataCollected(String message,String sender);
		/**
		 * Called in case of error
		 * 
		 * @param error
		 */
		public void onError(String error);
	}

	public DeviceDataReceiver(DataCollectionHandler listener) {
		this.handler = listener;
	}

	
	@Override
	public void run(){

		try{
			// Create a server socket and wait for client connections. This
			// call blocks until a connection is accepted from a client
			Log.d(TAG, this.getClass().getSimpleName()+": Tiro su un socket TCP");
			
			server_socket = new ServerSocket(8888);
			
			while(!terminated){

				// Waits for an incoming connection
				Socket client = server_socket.accept();
				
				Log.d(TAG, this.getClass().getSimpleName()+": Connessione in ingresso");

				// Read from the input stream
				InputStream inputstream = client.getInputStream();
				InputStreamReader is = new InputStreamReader(inputstream);
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(is);
				String read = br.readLine();

				while(read != null) {
					sb.append(read);
					read =br.readLine();
				}
				
				String xml_msg = new String(sb.toString());
				
				// call handler's onDataCollected method passing the message and sender's ip address
				handler.onDataCollected(xml_msg,client.getInetAddress().getCanonicalHostName());
				
				// Closing the client socket
				client.close();
			}

		}catch(Exception e){
			handler.onError(e.getMessage());
			Log.d(TAG, this.getClass().getSimpleName()+":Exception "+e.getMessage(),e);
		}
		
		// The last operation is closing the TCP socket
		finally{
			disconnect_socket();
		}
	}

	/**
	 * Method called to close the opened TCP socket if different to null and opened
	 * 
	 */
	private void disconnect_socket(){
		if(server_socket != null && !server_socket.isClosed()){
			try{
				server_socket.close();
				Log.d(TAG, this.getClass().getSimpleName()+": Ho chiuso il socket");
			}catch(Exception e){
				Log.d(TAG, this.getClass().getSimpleName()+": Impossibile chiudere il socket");
			}
		}
	}
	
	/**
	 * Method that can be used to stop thread execution
	 * 
	 * @param terminated
	 */
	public void stopDataReceiver(){
		this.terminated = true;
		// To stop the thread, set terminated to true and close the socket (if opened)
		disconnect_socket();
	}
}
