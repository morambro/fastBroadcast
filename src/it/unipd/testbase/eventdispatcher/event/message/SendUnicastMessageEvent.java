package it.unipd.testbase.eventdispatcher.event.message;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.wificonnection.message.IMessage;

public class SendUnicastMessageEvent implements IEvent {
	
	public IMessage message;
	public String recipient;
	
	public SendUnicastMessageEvent(IMessage message,String recipient) {
		this.message = message;
		this.recipient = recipient;
	}
}
