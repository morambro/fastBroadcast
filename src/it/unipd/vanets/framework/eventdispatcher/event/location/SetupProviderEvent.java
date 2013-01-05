package it.unipd.vanets.framework.eventdispatcher.event.location;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;

public class SetupProviderEvent implements IEvent {
	public int counter;
	public int peersNumber;
	
	public SetupProviderEvent(int counter, int peersNumber) {
		this.counter = counter;
		this.peersNumber = peersNumber;
	}
}
