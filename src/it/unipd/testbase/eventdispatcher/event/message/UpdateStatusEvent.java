package it.unipd.testbase.eventdispatcher.event.message;

import it.unipd.testbase.eventdispatcher.event.IEvent;

public class UpdateStatusEvent implements IEvent {
	public String status;
	
	public UpdateStatusEvent(String status) {
		this.status = status;
	}
}
