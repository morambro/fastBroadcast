package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;

import java.util.Map;


public interface IControllerComponent extends IComponent {
	boolean isGroupOwner();
	String getGroupOwnerAddress();
	void setPeersIdIPmap(Map<String,String> peersMap);
	void helloMessageArrived(IMessage message);
	void handleMessage(IMessage message);
	/**
	 * @return unique device id (MAC Address in this case)
	 */
	String getDeviceId();
	/**
	 * Tell whether register/unregister FastBroadcastReceiver
	 * 
	 * @param registered
	 */
	void setFastBroadCastReceiverRegistered(boolean b);
	
	/**
	 * broadcasts an alert message
	 */
	void sendAlert();
	
	/**
	 * Send the given message to all
	 * 
	 * @param message
	 */
	void sendBroadcast(IMessage message);
	/**
	 * Disconnects the current peer
	 */
	void disconnect();
	/**
	 * Calls connect for each device found
	 */
	void connectToAll();
}
