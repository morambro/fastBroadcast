package it.unipd.testbase.eventdispatcher.event.message;

import it.unipd.testbase.eventdispatcher.event.IEvent;

public class PartialFileIndexReceivedEvent implements IEvent {
	public int deviceID = -1;
	
	public PartialFileIndexReceivedEvent(int id) {
		deviceID = id;
	}
}
