package it.unipd.fast.broadcast.wifi_connection.receiver;


import it.unipd.fast.broadcast.IEvent;
import it.unipd.fast.broadcast.wifi_connection.receiver.protocols.UDPPacketReceiver;
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
public class DataReceiverService extends Service implements IDataReceiverComponent{
	protected final String TAG = "it.unipd.fast.broadcast";

	private AbstractPacketReceiver packetReceiver;// = new UDPPacketReceiver();


	public class DataReceiverBinder extends Binder {
		public IDataReceiverComponent getService() {
			return DataReceiverService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return new DataReceiverBinder();
	}
	
	@Override
	public void registerHandler(IDataCollectionHandler handler) {
		packetReceiver.add(handler);
	}

	@Override
	public void unregisterHandler(IDataCollectionHandler handler) {
		if(packetReceiver.remove(handler)) {
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
		packetReceiver.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void handle(IEvent event) {}	
}
