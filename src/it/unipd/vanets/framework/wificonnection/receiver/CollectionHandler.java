package it.unipd.vanets.framework.wificonnection.receiver;

import it.unipd.vanets.framework.AppController;
import it.unipd.vanets.framework.IControllerComponent;
import it.unipd.vanets.framework.AppController.IDataCollectionHandler;
import it.unipd.vanets.framework.eventdispatcher.EventDispatcher;
import it.unipd.vanets.framework.eventdispatcher.event.location.SetupProviderEvent;
import it.unipd.vanets.framework.eventdispatcher.event.message.UpdateStatusEvent;
import it.unipd.vanets.framework.eventdispatcher.event.protocol.NewMessageArrivedEvent;
import it.unipd.vanets.framework.helper.DebugLogger;
import it.unipd.vanets.framework.wificonnection.message.IMessage;

import java.util.HashMap;
import java.util.Map;

public class CollectionHandler implements IDataCollectionHandler {
	
	DebugLogger logger = new DebugLogger(CollectionHandler.class);
	
	private IControllerComponent controller = null;

	@Override
	public void onDataCollected(IMessage message, String hostIp) {
		
		int messageType = message.getType();

		if(message.getSenderID().equals(controller.getDeviceId())){
			logger.d("Message sent by me, discarded");
			return;
		}
		
		switch(messageType){
			// In case of Hello message 
			case IMessage.PING_MESSAGE_TYPE : 
				logger.d("Ricevuto  PING: \n"+message);
				
				EventDispatcher.getInstance().triggerEvent(new UpdateStatusEvent("PING message from "+message.getSenderID()));
				
				Map<String,String> peerData = new HashMap<String, String>();
				String client_id_address = message.getContent().get(IMessage.PING_MESSAGE_ID_KEY);
				peerData.put(client_id_address,hostIp);
				controller.setPeersIdIPmap(peerData);
				break;
	
				// In case of message MAP, for client addresses distribution
			case IMessage.CLIENT_MAP_MESSAGE_TYPE :
				logger.d("Ricevuto MAP \n: "+message);
				
				EventDispatcher.getInstance().triggerEvent(new UpdateStatusEvent("Received MAP message"));
				
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
				logger.d("Ricevuta lista");
				controller.setPeersIdIPmap(allPeerData);
				break;
			default:
				logger.d("Ricevuto MESSAGGIO : \n"+message);
				EventDispatcher.getInstance().triggerEvent(new NewMessageArrivedEvent(message,hostIp));
				break;
		}
	}

	@Override
	public void onError(String error) {
		logger.d(error);
	}

	@Override
	public void setWiFiController(AppController controller) {
		this.controller = controller;
	}
}
