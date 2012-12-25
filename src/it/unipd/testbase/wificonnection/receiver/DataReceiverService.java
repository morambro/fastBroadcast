package it.unipd.testbase.wificonnection.receiver;


import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.ShutdownEvent;
import it.unipd.testbase.helper.Log;
import it.unipd.testbase.wificonnection.receiver.protocols.RawPacketReceiver;

import java.util.ArrayList;
import java.util.List;


/**
 * Service used to receive data from other requestConnectionSent devices. It creates a Socket and waits 
 * for incoming connections.
 * 
 * @author Moreno Ambrosin
 *
 */
public class DataReceiverService implements IDataReceiverComponent{
	protected final String TAG = "it.unipd.testbase";

	private AbstractPacketReceiver rawPacketReceiver = null;

	private static DataReceiverService instance = null;

	public static DataReceiverService getInstance() {
		if(instance == null) {
			instance = new DataReceiverService();
		}
		return instance;
	}

	public void terminate() {
		rawPacketReceiver.terminate();
	}

	protected DataReceiverService() {
		Log.d(TAG, this.getClass().getSimpleName()+": Servizio creato");
		if(rawPacketReceiver == null)
		{
			register();
			rawPacketReceiver = new RawPacketReceiver();
			rawPacketReceiver.start();
		}
	}

	@Override
	public void handle(IEvent event) {
		if(event instanceof ShutdownEvent)
		{
			rawPacketReceiver.terminate();
			Log.d(TAG, this.getClass().getSimpleName()+": shutdown");
		}
		return;
	}


	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(ShutdownEvent.class);
		EventDispatcher.getInstance().registerComponent(this, events);
	}
}
