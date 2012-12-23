package it.unipd.testbase;

import it.unipd.testbase.eventdispatcher.IComponent;

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
	 * @return unique device id (MAC Address in this case)
	 */
	String getDeviceId();
	/**
	 * Tell whether register/unregister WifiBroadcastReceiver
	 * 
	 * @param registered
	 */
	void setFastBroadCastReceiverRegistered(boolean b);
	
	/**
	 * broadcasts an alert message
	 */
//	void sendAlert();
	
	/**
	 * Disconnects the current peer
	 */
	void disconnect();
	/**
	 * Calls connect for each device found
	 */
	void connectToAll();
}
