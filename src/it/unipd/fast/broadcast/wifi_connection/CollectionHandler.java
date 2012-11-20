package it.unipd.fast.broadcast.wifi_connection;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService.IDataCollectionHandler;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class CollectionHandler implements IDataCollectionHandler {
	private static final String TAG = "it.unipd.fast.broadcast";
	private WiFiConnectionController controller = null;

	public void onDataCollected(IMessage message, String host_ip) {
		String recipient = message.getRecipientAddress();
		String currentIp = WiFiUtility.getDottedDecimalIP(WiFiUtility.getLocalIPAddress());
		currentIp = currentIp.replace(".", "");
		if(rerouteMessage(currentIp, recipient, message))
			return;
		int message_type = message.getType();
		Log.d(TAG, this.getClass().getSimpleName()+": Message received, of type = "+message_type);
		switch(message_type){

		// In case of Hello message 
		case HELLO_MESSAGE_TYPE : 
			Map<String,String> peer_data = new HashMap<String, String>();
			String client_id_address = message.getContent().get(IMessage.PING_MESSAGE_ID_KEY);
			peer_data.put(client_id_address,host_ip);
			controller.setPeersIdIPmap(peer_data);
			break;

			// In case of message MAP, for client addresses distribution
		case CLIENT_MAP_MESSAGE_TYPE :
			Map<String,String> all_peer_data = new HashMap<String, String>();
			all_peer_data.putAll(message.getContent());
			Log.d(TAG, this.getClass().getSimpleName()+": Ricevuta lista");
			controller.setPeersIdIPmap(all_peer_data);
			break;

		case ALERT_MESSAGE_TYPE :
			Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto : "+message);
			break;

		default : 
			Log.d(TAG, this.getClass().getSimpleName()+": Unknown message type "+message_type+", discarded.");
			// TODO : unknown message type error handling
		}
	}

	private boolean rerouteMessage(String currentIp, String recipient, IMessage msg) {
		Log.d(TAG, this.getClass().getSimpleName()+": comparing IPs; "+currentIp+" "+recipient);
		if(currentIp.equals(recipient))
			return false;
		TransmissionManagerFactory.getInstance().getTransmissionManager().send(recipient, msg);
		return true;
	}

	public void onError(String error) {
		Log.d(TAG, this.getClass().getSimpleName()+": "+error);
	}

	@Override
	public void setWiFiController(WiFiConnectionController controller) {
		this.controller = controller;
	}
}
