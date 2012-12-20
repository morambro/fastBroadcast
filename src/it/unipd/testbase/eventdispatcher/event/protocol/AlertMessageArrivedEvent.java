package it.unipd.testbase.eventdispatcher.event.protocol;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.wificonnection.message.IMessage;

public class AlertMessageArrivedEvent implements IEvent {
	public IMessage alertMessage;
	
	public AlertMessageArrivedEvent(IMessage alertIMessage){
		this.alertMessage = alertIMessage;
	}
}
