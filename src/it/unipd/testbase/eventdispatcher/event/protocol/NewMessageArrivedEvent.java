package it.unipd.testbase.eventdispatcher.event.protocol;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.wificonnection.message.IMessage;

public class NewMessageArrivedEvent implements IEvent {
	public IMessage message;
	public String senderID;
	
	public NewMessageArrivedEvent(IMessage alertIMessage, String senderID){
		this.message = alertIMessage;
		this.senderID = senderID;
	}
}
