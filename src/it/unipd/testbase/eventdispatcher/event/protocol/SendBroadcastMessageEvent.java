package it.unipd.testbase.eventdispatcher.event.protocol;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.wificonnection.message.IMessage;

public class SendBroadcastMessageEvent implements IEvent {
	public IMessage message;
	
	public SendBroadcastMessageEvent(IMessage message) {
		this.message 		= message;
	}
}
