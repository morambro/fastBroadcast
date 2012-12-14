package it.unipd.fast.broadcast.wificonnection.receiver;


import it.unipd.fast.broadcast.event.IEvent;
import it.unipd.fast.broadcast.wificonnection.receiver.protocols.UDPPacketReceiver;
import android.util.Log;


/**
 * Service used to receive data from other requestConnectionSent devices. It creates a Socket and waits 
 * for incoming connections.
 * 
 * @author Moreno Ambrosin
 *
 */
public class DataReceiverService implements IDataReceiverComponent{
	protected final String TAG = "it.unipd.fast.broadcast";

	private AbstractPacketReceiver packetReceiver;// = new UDPPacketReceiver();
	private static DataReceiverService instance = null;
	
	public static DataReceiverService getInstance() {
		if(instance == null)
			instance = new DataReceiverService();
		return instance;
	}
	
	public void terminate() {
		packetReceiver.terminate();
	}

	protected DataReceiverService() {
		Log.d(TAG, this.getClass().getSimpleName()+": Servizio creato");
		packetReceiver = new UDPPacketReceiver();
		packetReceiver.start();
	}

	@Override
	public void handle(IEvent event) {}

	@Override
	public void register() {}	
}
