package it.unipd.fast.broadcast.wifi_connection.receiver;

import it.unipd.fast.broadcast.AppController;
import it.unipd.fast.broadcast.wifi_connection.message.IMessage;

public interface IDataReceiverService {
	/**
	 * Interface used to specify operation to do on data collected or when an error occurs
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public static interface IDataCollectionHandler {
		public void setWiFiController(AppController controller);
		public void onDataCollected(IMessage message,String sender);
		public void onError(String error);
	}
	
	/**
	 * Adds a new handler to data receiver service
	 * 
	 * @param handler
	 */
	void registerHandler(IDataCollectionHandler handler);
	/**
	 * unregisters the specified handler
	 * 
	 * @param handler
	 */
	void unregisterHandler(IDataCollectionHandler handler);
}
