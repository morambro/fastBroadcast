package it.unipd.fast.broadcast.location;

import android.location.Location;
import it.unipd.fast.broadcast.IEvent;

public class LocationChangedEvent implements IEvent {
	public Location location;
	
	public LocationChangedEvent(Location location) {
		this.location = location;
	}
}
