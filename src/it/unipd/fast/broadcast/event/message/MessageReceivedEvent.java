package it.unipd.fast.broadcast.event.message;

import it.unipd.fast.broadcast.event.IEvent;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;

public class MessageReceivedEvent implements IEvent {
	public IMessage message;
	public String senderID;
	
	public MessageReceivedEvent(IMessage message,String senderID) {
		this.message = message;
		this.senderID = senderID;
	}
}
