package it.unipd.fast.broadcast.wifi_connection.receiver;

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


/**
 * Service used to receive data from other connected devices. It creates a Socket and waits 
 * for incoming connections.
 * 
 * @author Moreno Ambrosin
 *
 */
public class DataReceiverService extends Service implements Runnable, IDataReceiverService{
	protected final String TAG = "it.unipd.fast.broadcast";

	private boolean terminated = false;
	private ServerSocket serverSocket;
	private List<IDataCollectionHandler> handlers = new ArrayList<IDataCollectionHandler>();
	


	public class DataReceiverBinder extends Binder {
		public IDataReceiverService getService() {
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
			disconnectSocket();
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
			serverSocket = new ServerSocket(8888);
			while(!terminated){
				
				// Waits for an incoming connection
				Socket client = serverSocket.accept();
				Log.d(TAG, this.getClass().getSimpleName()+": connessione in ingresso");
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
			disconnectSocket();
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

					// call handler's onDataCollected method passing the message and sender's ip address
					Log.d(TAG, this.getClass().getSimpleName()+": handlers = "+handlers);
					for (IDataCollectionHandler handler : handlers) {
						handler.onDataCollected(
								MessageBuilder.getInstance().getMessage(xmlMsg),
								socket.getInetAddress().getCanonicalHostName()
						);
					}
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	/**
	 * Method called to close the opened TCP socket if different to null and opened
	 * 
	 */
	private void disconnectSocket(){
		if(serverSocket != null && !serverSocket.isClosed()){
			try{
				serverSocket.close();
				Log.d(TAG, this.getClass().getSimpleName()+": Ho chiuso il socket");
			}catch(Exception e){
				Log.d(TAG, this.getClass().getSimpleName()+": Impossibile chiudere il socket");
			}
		}
	}
}
