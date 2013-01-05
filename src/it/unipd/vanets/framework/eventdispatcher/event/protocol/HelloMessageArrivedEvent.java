package it.unipd.vanets.framework.eventdispatcher.event.protocol;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.wificonnection.message.IMessage;

public class HelloMessageArrivedEvent implements IEvent{
	public IMessage message;
	
	public HelloMessageArrivedEvent(IMessage message) {
		this.message = message;
	}
}
