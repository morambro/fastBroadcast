package it.unipd.fast.broadcast.wifi_connection;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;

import java.util.Map;


public interface IWiFiConnectionController {
	boolean isGroupOwner();
	String getGroupOwnerAddress();
	void setPeersIdIPmap(Map<String,String> peersMap);
	void helloMessageArrived(IMessage message);
	/**
	 * @return unique device id (MAC Address in this case)
	 */
	String getDeviceId();
}
