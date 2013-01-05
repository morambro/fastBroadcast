package it.unipd.vanets.framework.eventdispatcher.event.message;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.wificonnection.message.IMessage;

public class SendUnicastMessageEvent implements IEvent {
	
	public IMessage message;
	public String recipient;
	
	public SendUnicastMessageEvent(IMessage message,String recipient) {
		this.message = message;
		this.recipient = recipient;
	}
}
