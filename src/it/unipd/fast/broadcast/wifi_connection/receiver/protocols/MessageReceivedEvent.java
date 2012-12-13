package it.unipd.fast.broadcast.wifi_connection.receiver.protocols;

import it.unipd.fast.broadcast.IEvent;
import it.unipd.fast.broadcast.wifi_connection.message.IMessage;

public class MessageReceivedEvent implements IEvent {
	public IMessage message;
	public String senderID;
	
	public MessageReceivedEvent(IMessage message,String senderID) {
		this.message = message;
		this.senderID = senderID;
	}
}
