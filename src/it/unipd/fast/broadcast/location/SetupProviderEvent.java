package it.unipd.fast.broadcast.location;

import it.unipd.fast.broadcast.IEvent;

public class SetupProviderEvent implements IEvent {
	public int counter;
	public int peersNumber;
	
	public SetupProviderEvent(int counter, int peersNumber) {
		this.counter = counter;
		this.peersNumber = peersNumber;
	}
}
