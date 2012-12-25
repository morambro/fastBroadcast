package it.unipd.testbase.eventdispatcher.event.location;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.location.Location;

public class LocationChangedEvent implements IEvent {
	public Location location;
	
	public LocationChangedEvent(Location location) {
		this.location = location;
	}
}
