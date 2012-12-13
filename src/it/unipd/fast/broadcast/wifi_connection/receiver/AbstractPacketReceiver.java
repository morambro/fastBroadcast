package it.unipd.fast.broadcast.wifi_connection.receiver;


import it.unipd.fast.broadcast.wifi_connection.receiver.IDataReceiverComponent.IDataCollectionHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPacketReceiver implements Runnable{
	protected final String TAG = "it.unipd.fast.broadcast";
	
	protected List<IDataCollectionHandler> handlers;
	protected boolean terminated = false;
	
	public void start(){
		new Thread(this).start();
	}
	
	abstract public void terminate();
	
	public void add(IDataCollectionHandler handler){
		if(handlers == null)
			handlers = new ArrayList<IDataCollectionHandler>();
		handlers.add(handler);
	}
	
	/**
	 * 
	 * @param handler
	 * @return true if handlers.size == 0
	 */
	public boolean remove(IDataCollectionHandler handler){
		handlers.remove(handler);
		if(handlers.size()==0)
			return true;
		return false;
	}
}
