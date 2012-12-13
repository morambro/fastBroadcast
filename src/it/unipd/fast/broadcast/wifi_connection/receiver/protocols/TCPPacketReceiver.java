package it.unipd.fast.broadcast.wifi_connection.receiver.protocols;

import it.unipd.fast.broadcast.EventDispatcher;
import it.unipd.fast.broadcast.event.message.MessageReceivedEvent;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.receiver.AbstractPacketReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class TCPPacketReceiver extends AbstractPacketReceiver{
	protected final String TAG = "it.unipd.fast.broadcast";
	private ServerSocket serverSocket;
	
	@Override
	public void run() {
		try{
			// Create a server socket and wait for client connections. This
			// call blocks until a connection is accepted from a client
			Log.d(TAG, this.getClass().getSimpleName()+": Tiro su un socket TCP terminated = ");
			serverSocket = new ServerSocket(8888);
			
			while(!terminated){
				Log.d(TAG, TCPPacketReceiver.class.getSimpleName()+": Attendo una connessione");
				// Waits for an incoming connection
				Socket client = serverSocket.accept();
				Log.d(TAG, TCPPacketReceiver.class.getSimpleName()+": connessione in ingresso");
				handleConnection(client);
			}
		}catch(IOException e){
			// TODO : Introduce Error Event to be propagated
			Log.d(TAG, TCPPacketReceiver.class.getSimpleName()+":Exception "+e.getMessage(),e);
		}

		// close the TCP socket before exiting
		finally{
			disconnectSocket();
		}
	}
	
	/**
	 * Method called to close the opened TCP socket if different to null and opened
	 * 
	 */
	private void disconnectSocket(){
		if(serverSocket != null && !serverSocket.isClosed()){
			try{
				serverSocket.close();
				Log.d(TAG, TCPPacketReceiver.class.getSimpleName()+": Ho chiuso il socket");
			}catch(Exception e){
				Log.d(TAG, TCPPacketReceiver.class.getSimpleName()+": Impossibile chiudere il socket");
			}
		}
	}
	
	private void handleConnection(final Socket socket){
		new Thread() {
			
			@Override
			public void run() {
				Log.d(TAG, this.getClass().getSimpleName()+": Gestione connessione in nuovo thread");
				// Read from the input stream
				try {
					InputStream inputstream = socket.getInputStream();
					InputStreamReader is = new InputStreamReader(inputstream);
					StringBuilder sb = new StringBuilder();
					BufferedReader br = new BufferedReader(is);
					String read = br.readLine();

					while(read != null) {
						sb.append(read);
						read =br.readLine();
					}

					String xmlMsg = new String(sb.toString());

					EventDispatcher.getInstance().triggerEvent(new MessageReceivedEvent(
							MessageBuilder.getInstance().getMessage(xmlMsg),
							socket.getInetAddress().getCanonicalHostName()
					));
					
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	@Override
	public void terminate() {
		this.terminated = true;
	}
}
