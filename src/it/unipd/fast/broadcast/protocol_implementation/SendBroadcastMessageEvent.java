package it.unipd.fast.broadcast.protocol_implementation;

import it.unipd.fast.broadcast.IEvent;
import it.unipd.fast.broadcast.wifi_connection.message.IMessage;

public class SendBroadcastMessageEvent implements IEvent {
	public IMessage message;
	
	public SendBroadcastMessageEvent(IMessage message) {
		this.message = message;
	}
}
