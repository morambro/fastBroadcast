package it.unipd.fastbroadcast.event;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;

public class SendAlertMessageEvent implements IEvent {

	public int hops = 0;
	
	public SendAlertMessageEvent(int hops) {
		this.hops = hops;
	}
}
