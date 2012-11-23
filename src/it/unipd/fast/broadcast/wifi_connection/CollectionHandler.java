package it.unipd.fast.broadcast.wifi_connection;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService.IDataCollectionHandler;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class CollectionHandler implements IDataCollectionHandler {
	private static final String TAG = "it.unipd.fast.broadcast";
	private IWiFiConnectionController controller = null;

	public void onDataCollected(IMessage message, String host_ip) {
		String recipient = message.getRecipientAddress();
		
		rerouteMessage(recipient, message);
		
		int messageType = message.getType();
		Log.d(TAG, this.getClass().getSimpleName()+": Message received, of type = "+messageType);
		switch(messageType){
			// In case of Hello message 
			case IMessage.PING_MESSAGE_TYPE : 
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  PING: \n"+message);
				Map<String,String> peerData = new HashMap<String, String>();
				String client_id_address = message.getContent().get(IMessage.PING_MESSAGE_ID_KEY);
				peerData.put(client_id_address,host_ip);
				controller.setPeersIdIPmap(peerData);
				break;
	
				// In case of message MAP, for client addresses distribution
			case IMessage.CLIENT_MAP_MESSAGE_TYPE :
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto MAP \n: "+message);
				Map<String,String> allPeerData = new HashMap<String, String>();
				allPeerData.putAll(message.getContent());
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuta lista");
				controller.setPeersIdIPmap(allPeerData);
				break;
	
			case IMessage.ALERT_MESSAGE_TYPE :
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto ALERT : \n"+message);
				break;
				
			// case of fast broadcast hello message
			case IMessage.HELLO_MESSAGE_TYPE :
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  HELLO : \n"+message);
				controller.helloMessageArrived(message);
				break;
				
			// Ignoring unknown messages
			default : 
				Log.d(TAG, this.getClass().getSimpleName()+": Unknown message type "+messageType+", discarded.");
		}
	}

	/**
	 * Method used to understand whether to reroute a message or not
	 * 
	 * @param recipient
	 * @param msg
	 * @return
	 */
	private boolean rerouteMessage(String recipient, IMessage msg) {
		Log.d(TAG, this.getClass().getSimpleName()+": comparing IPs; "+controller.getGroupOwnerAddress()+" "+recipient);
		if(controller.isGroupOwner())
			if(!controller.getGroupOwnerAddress().equals(recipient) && !recipient.equals(IMessage.BROADCAST_ADDRESS)) {
				Log.d(TAG, this.getClass().getSimpleName()+": comparing IPs; "+controller.getGroupOwnerAddress()+" "+recipient);
				TransmissionManagerFactory.getInstance().getTransmissionManager().send(recipient, msg);
				return true;
			}
		return false;
	}

	public void onError(String error) {
		Log.d(TAG, this.getClass().getSimpleName()+": "+error);
	}

	@Override
	public void setWiFiController(WiFiConnectionController controller) {
		this.controller = controller;
	}
}
