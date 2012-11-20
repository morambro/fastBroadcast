package it.unipd.fast.broadcast.wifi_connection.transmissionmanager;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;

import java.util.List;

public interface ITranmissionManager {
	static final String BROADCAST_ADDRESS = "0";
	
	void send(String ID, IMessage msg);
	void send(List<String> IDs, IMessage msg);
}
