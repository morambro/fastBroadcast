package it.unipd.vanets.framework.eventdispatcher.event.protocol;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.wificonnection.message.IMessage;

public class SendBroadcastMessageEvent implements IEvent {
	public IMessage message;
	
	public SendBroadcastMessageEvent(IMessage message) {
		this.message 		= message;
	}
}
