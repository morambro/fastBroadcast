package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.wificonnection.message.IMessage;

public interface ITranmissionManager {
	
	static final String BROADCAST_ADDRESS = "ff:ff:ff:ff";
	
	void send(String ID, IMessage msg);
	void release();
}
