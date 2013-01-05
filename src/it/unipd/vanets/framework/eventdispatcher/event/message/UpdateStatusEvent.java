package it.unipd.vanets.framework.eventdispatcher.event.message;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;

public class UpdateStatusEvent implements IEvent {
	public String status;
	
	public UpdateStatusEvent(String status) {
		this.status = status;
	}
}
