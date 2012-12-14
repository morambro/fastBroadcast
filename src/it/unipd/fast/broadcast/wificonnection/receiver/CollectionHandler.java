package it.unipd.fast.broadcast.wificonnection.receiver;

import it.unipd.fast.broadcast.AppController;
import it.unipd.fast.broadcast.AppController.IDataCollectionHandler;
import it.unipd.fast.broadcast.EventDispatcher;
import it.unipd.fast.broadcast.IControllerComponent;
import it.unipd.fast.broadcast.event.location.SetupProviderEvent;
import it.unipd.fast.broadcast.event.protocol.AlertMessageArrivedEvent;
import it.unipd.fast.broadcast.event.protocol.HelloMessageArrivedEvent;
import it.unipd.fast.broadcast.helper.LogPrinter;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;
import it.unipd.fast.broadcast.wificonnection.transmissionmanager.TransmissionManagerFactory;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class CollectionHandler implements IDataCollectionHandler {
	private static final String TAG = "it.unipd.fast.broadcast";
	
	private IControllerComponent controller = null;

	public void onDataCollected(IMessage message, String hostIp) {
		String recipient = message.getRecipientAddress();
		Log.d(TAG, this.getClass().getSimpleName()+": Message body: "+(new String(message.getMessage())));
		
		rerouteMessage(recipient, message);
		
		int messageType = message.getType();
		Log.d(TAG, this.getClass().getSimpleName()+": Message received, of type = "+messageType);
		switch(messageType){
			// In case of Hello message 
			case IMessage.PING_MESSAGE_TYPE : 
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  PING: \n"+message);
				Map<String,String> peerData = new HashMap<String, String>();
				String client_id_address = message.getContent().get(IMessage.PING_MESSAGE_ID_KEY);
				peerData.put(client_id_address,hostIp);
				controller.setPeersIdIPmap(peerData);
				break;
	
				// In case of message MAP, for client addresses distribution
			case IMessage.CLIENT_MAP_MESSAGE_TYPE :
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto MAP \n: "+message);
				Map<String,String> allPeerData = new HashMap<String, String>();
				Map<String, String> content = message.getContent();
				for (String key : content.keySet()) {
					String msgContent = content.get(key);
					if(key.equals(controller.getDeviceId()))
						EventDispatcher.getInstance().triggerEvent(new SetupProviderEvent(Integer.parseInt
								(IMessage.splitContent(msgContent)[IMessage.FILE_COUNTER_INDEX]), content.size()));
						//MockLocationProvider.__set_static_couter(Integer.parseInt(IMessage.splitContent(msgContent)[IMessage.FILE_COUNTER_INDEX]), content.size());
					allPeerData.put(key, IMessage.splitContent(msgContent)[0]);
				}
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuta lista");
				controller.setPeersIdIPmap(allPeerData);
				break;
	
			case IMessage.ALERT_MESSAGE_TYPE :
				LogPrinter.getInstance().writeTimedLine("alert message received from "+hostIp+". Hop number: "+message.getContent().get(IMessage.MESSAGE_HOP_KEY));
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto ALERT : \n"+message);
				EventDispatcher.getInstance().triggerEvent(new AlertMessageArrivedEvent(message));
				break;
				
			// case of fast broadcast hello message
			case IMessage.HELLO_MESSAGE_TYPE :
				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  HELLO : \n"+message);
				EventDispatcher.getInstance().triggerEvent(new HelloMessageArrivedEvent(message));
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
	public void setWiFiController(AppController controller) {
		this.controller = controller;
	}
}
