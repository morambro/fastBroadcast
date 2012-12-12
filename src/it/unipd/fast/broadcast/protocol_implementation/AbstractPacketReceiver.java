package it.unipd.fast.broadcast.protocol_implementation;

import it.unipd.fast.broadcast.wifi_connection.receiver.IDataReceiverService.IDataCollectionHandler;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public abstract class AbstractPacketReceiver implements Runnable{
	protected final String TAG = "it.unipd.fast.broadcast";
	
	protected List<IDataCollectionHandler> handlers;
	protected boolean terminated = false;
	
	public void start(List<IDataCollectionHandler> handlers){
		this.handlers = handlers;
		Log.d(TAG,this.getClass().getSimpleName() + " : handlers = "+handlers.size());
		new Thread(this).start();
	}
	
	abstract public void terminate();
	
	public void add(IDataCollectionHandler handler){
		if(handlers == null) handlers = new ArrayList<IDataCollectionHandler>();
		handlers.add(handler);
	}
	
	public void remove(IDataCollectionHandler handler){
		handlers.remove(handler);
	}
}
