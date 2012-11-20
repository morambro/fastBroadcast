package it.unipd.fast.broadcast.location;

import android.location.Location;

//listener to register in LocationService to obtain location updates
public interface LocationServiceListener {
	void onLocationChanged(Location location);
}
