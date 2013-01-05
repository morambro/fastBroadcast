package it.unipd.vanets.framework.eventdispatcher.event.message;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.wificonnection.message.IMessage;

public class MessageReceivedEvent implements IEvent {
	public IMessage message;
	public String senderID;
	
	public MessageReceivedEvent(IMessage message,String senderID) {
		this.message = message;
		this.senderID = senderID;
	}
}
