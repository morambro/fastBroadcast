package it.unipd.vanets.framework.eventdispatcher.event.location;

import android.location.Location;
import it.unipd.vanets.framework.eventdispatcher.event.IEvent;

public class LocationChangedEvent implements IEvent {
	public Location location;
	
	public LocationChangedEvent(Location location) {
		this.location = location;
	}
}
