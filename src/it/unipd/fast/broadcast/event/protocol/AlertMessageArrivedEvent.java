package it.unipd.fast.broadcast.event.protocol;

import it.unipd.fast.broadcast.event.IEvent;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;

public class AlertMessageArrivedEvent implements IEvent {
	public IMessage alertMessage;
	
	public AlertMessageArrivedEvent(IMessage alertIMessage){
		this.alertMessage = alertIMessage;
	}
}
