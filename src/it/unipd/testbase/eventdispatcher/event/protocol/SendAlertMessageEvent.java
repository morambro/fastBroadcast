package it.unipd.testbase.eventdispatcher.event.protocol;

import it.unipd.testbase.eventdispatcher.event.IEvent;

public class SendAlertMessageEvent implements IEvent {

	public int hops = 0;
	
	public SendAlertMessageEvent(int hops) {
		this.hops = hops;
	}
}
