package it.unipd.testbase.eventdispatcher.event.location;

import it.unipd.testbase.eventdispatcher.event.IEvent;

public class SetupProviderEvent implements IEvent {
	public int counter;
	public int peersNumber;
	
	public SetupProviderEvent(int counter, int peersNumber) {
		this.counter = counter;
		this.peersNumber = peersNumber;
	}
}
