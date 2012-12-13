package it.unipd.fast.broadcast.wifi_connection.receiver;


import it.unipd.fast.broadcast.wifi_connection.receiver.protocols.TCPPacketReceiver;
import it.unipd.fast.broadcast.wifi_connection.receiver.protocols.UDPPacketReceiver;

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
public class DataReceiverService extends Service implements IDataReceiverService{
	protected final String TAG = "it.unipd.fast.broadcast";

	private List<IDataCollectionHandler> handlers = new ArrayList<IDataCollectionHandler>();
	private AbstractPacketReceiver packetReceiver;// = new UDPPacketReceiver();


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
		packetReceiver.add(handler);
	}

	@Override
	public void unregisterHandler(IDataCollectionHandler handler) {
		handlers.remove(handler);
		packetReceiver.remove(handler);
		if(handlers.size()==0) {
			packetReceiver.terminate();
			Log.d(TAG, this.getClass().getSimpleName()+": Service terminated");
			stopSelf();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, this.getClass().getSimpleName()+": Servizio creato");
		packetReceiver = new UDPPacketReceiver();
		packetReceiver.start(handlers);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}	
}
