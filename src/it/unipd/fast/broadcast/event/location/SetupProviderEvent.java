package it.unipd.fast.broadcast.event.location;

import it.unipd.fast.broadcast.event.IEvent;

public class SetupProviderEvent implements IEvent {
	public int counter;
	public int peersNumber;
	
	public SetupProviderEvent(int counter, int peersNumber) {
		this.counter = counter;
		this.peersNumber = peersNumber;
	}
}
