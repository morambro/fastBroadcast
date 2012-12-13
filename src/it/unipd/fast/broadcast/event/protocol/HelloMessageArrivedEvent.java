package it.unipd.fast.broadcast.event.protocol;

import it.unipd.fast.broadcast.event.IEvent;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;

public class HelloMessageArrivedEvent implements IEvent{
	public IMessage message;
	
	public HelloMessageArrivedEvent(IMessage message) {
		this.message = message;
	}
}
