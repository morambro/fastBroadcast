package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.wificonnection.message.IMessage;

import java.util.Map;


public interface IControllerComponent extends IComponent {
	/**
	 * Tells whether current device is WI-FI Direct group owner
	 * 
	 * @return
	 */
	boolean isGroupOwner();
	/**
	 * Returns group owner address
	 * 
	 * @return
	 */
	String getGroupOwnerAddress();
	/**
	 * Setter method for Peers Map
	 * 
	 * @param peersMap
	 */
	void setPeersIdIPmap(Map<String,String> peersMap);
	/**
	 * Method called when a message arrives
	 * 
	 * @param message
	 */
	void helloMessageArrived(IMessage message);
	/**
	 * Handles a message
	 * 
	 * @param message
	 */
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
