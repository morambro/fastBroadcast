package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.IComponent;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.ShutdownEvent;
import it.unipd.testbase.helper.Log;

import java.util.ArrayList;
import java.util.List;


public class TransmissionManagerFactory implements IComponent {
	protected static final String TAG = "it.unipd.testbase";

	private static TransmissionManagerFactory singleton = null;
	private static ITranmissionManager tr = null;

	private TransmissionManagerFactory() {
		register();
	}

	public static TransmissionManagerFactory getInstance() {
		if(singleton == null)
			singleton = new TransmissionManagerFactory();
		return singleton;
	}

	public ITranmissionManager getTransmissionManager() {
		if(tr == null)
			tr = new RawTransmissionManager();
		return tr;
	}

	@Override
	public void handle(IEvent event) {
		if(event instanceof ShutdownEvent) {
			Log.d(TAG, this.getClass().getSimpleName()+": shutdown");
			if(tr!=null)
				tr.release();
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
