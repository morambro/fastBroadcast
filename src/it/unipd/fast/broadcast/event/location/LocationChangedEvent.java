package it.unipd.fast.broadcast.event.location;

import android.location.Location;
import it.unipd.fast.broadcast.event.IEvent;

public class LocationChangedEvent implements IEvent {
	public Location location;
	
	public LocationChangedEvent(Location location) {
		this.location = location;
	}
}