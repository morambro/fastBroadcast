package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.wificonnection.message.IMessage;

import java.util.List;

public interface ITranmissionManager {
	
	static final int UDP_PORT = 8889;
	static final int TCP_PORT = 8888;
	
	static final String BROADCAST_ADDRESS = "192.168.49.255";
	
	void send(String ID, IMessage msg);
	
	void send(List<String> IDs, IMessage msg);
}
