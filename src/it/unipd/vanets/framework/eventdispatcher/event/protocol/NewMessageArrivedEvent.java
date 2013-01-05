package it.unipd.vanets.framework.eventdispatcher.event.protocol;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.wificonnection.message.IMessage;

public class NewMessageArrivedEvent implements IEvent {
	public IMessage message;
	public String senderID;
	
	public NewMessageArrivedEvent(IMessage alertIMessage,String senderID){
		this.message = alertIMessage;
		this.senderID = senderID;
	}
}
