package it.unipd.fast.broadcast.wifi_connection.receiver;

import it.unipd.fast.broadcast.wifi_connection.WiFiConnectionController;
import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DataReceiverService extends Service implements Runnable, DataReceiverServiceInterface{
	protected final String TAG = "it.unipd.fast.broadcast";

	/**
	 * UDP packet size, 1KByte
	 */
	public static final int PACKET_SIZE = 1024;
	private boolean terminated = false;
	private ServerSocket server_socket;
	private List<IDataCollectionHandler> handlers = new ArrayList<DataReceiverService.IDataCollectionHandler>();
	
	/**
	 * Interface used to specify operation to do on data collected or when an error occurs
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public static interface IDataCollectionHandler {
		//Hello message used to collect IP addresses
		public static final int HELLO_MESSAGE_TYPE 		= 0;

		//Message used to share IP address of the network peers
		public static final int CLIENT_MAP_MESSAGE_TYPE = 1;

		//Message used to send an ALERT message
		public static final int ALERT_MESSAGE_TYPE 		= 2;

		public void setWiFiController(WiFiConnectionController controller);
		public void onDataCollected(IMessage message,String sender);
		public void onError(String error);
	}


	public class DataReceiverBinder extends Binder {
		public DataReceiverServiceInterface getService() {
			return DataReceiverService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new DataReceiverBinder();
	}
	
	@Override
	public void registerHandler(IDataCollectionHandler handler) {
		handlers.add(handler);
	}

	@Override
	public void unregisterHandler(IDataCollectionHandler handler) {
		handlers.remove(handler);
		if(handlers.size()==0) {
			Log.d(TAG, this.getClass().getSimpleName()+": Service terminated");
			stopSelf();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, this.getClass().getSimpleName()+": Servizio creato");
		new Thread(this).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
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
				Log.d(TAG, this.getClass().getSimpleName()+": Incoming Socket ;)");
				handleConnection(client);
			}
		}catch(IOException e){
			for (IDataCollectionHandler handler : handlers) {
				handler.onError(e.getMessage());
			}
			Log.d(TAG, this.getClass().getSimpleName()+":Exception "+e.getMessage(),e);
		}
		
		// close the TCP socket before exiting
		finally{
			disconnect_socket();
		}
	}
	
	private void handleConnection(final Socket socket){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Log.d(TAG, this.getClass().getSimpleName()+": Gestione connessione in nuovo thread");
				// Read from the input stream
				InputStream inputstream;
				try {
					inputstream = socket.getInputStream();
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
				for (IDataCollectionHandler handler : handlers) {
					handler.onDataCollected(MessageBuilder.getInstance().getMessage(xml_msg),socket.getInetAddress().getCanonicalHostName());
				}
				socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
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
}
