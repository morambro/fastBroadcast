package it.unipd.fast.broadcast.wifi_connection.receiver;

import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService.IDataCollectionHandler;

public interface DataReceiverServiceInterface {
	void registerHandler(IDataCollectionHandler handler);
	void unregisterHandler(IDataCollectionHandler handler);
}
