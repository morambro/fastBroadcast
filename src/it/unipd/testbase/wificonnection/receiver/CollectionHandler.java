package it.unipd.testbase.wificonnection.receiver;

import it.unipd.testbase.AppController;
import it.unipd.testbase.IControllerComponent;
import it.unipd.testbase.AppController.IDataCollectionHandler;
import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.location.SetupProviderEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.AlertMessageArrivedEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.HelloMessageArrivedEvent;
import it.unipd.testbase.helper.LogPrinter;
import it.unipd.testbase.wificonnection.message.IMessage;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class CollectionHandler implements IDataCollectionHandler {
	private static final String TAG = "it.unipd.testbase";
	
	private IControllerComponent controller = null;

	@Override
	public void onDataCollected(IMessage message, String hostIp) {
		
		int messageType = message.getType();
		Log.d(TAG, this.getClass().getSimpleName()+": Message received, of type = "+messageType);
		
		if(message.getSenderID().equals(controller.getDeviceId())){
			Log.d(TAG, this.getClass().getSimpleName()+": Message Discarded");
			return;
		}
		
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
				LogPrinter.getInstance().writeTimedLine("------------------------------------\n");
				LogPrinter.getInstance().writeTimedLine("ALERT FROM "+hostIp+". HOPS "+message.getContent().get(IMessage.MESSAGE_HOP_KEY));
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

	@Override
	public void onError(String error) {
		Log.d(TAG, this.getClass().getSimpleName()+": "+error);
	}

	@Override
	public void setWiFiController(AppController controller) {
		this.controller = controller;
	}
}
