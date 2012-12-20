package it.unipd.testbase.eventdispatcher.event.message;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.wificonnection.message.IMessage;

public class MessageReceivedEvent implements IEvent {
	public IMessage message;
	public String senderID;
	
	public MessageReceivedEvent(IMessage message,String senderID) {
		this.message = message;
		this.senderID = senderID;
	}
}
