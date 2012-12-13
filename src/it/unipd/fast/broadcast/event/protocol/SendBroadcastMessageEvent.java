package it.unipd.fast.broadcast.event.protocol;

import it.unipd.fast.broadcast.event.IEvent;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;

public class SendBroadcastMessageEvent implements IEvent {
	public IMessage message;
	
	public SendBroadcastMessageEvent(IMessage message) {
		this.message = message;
	}
}
