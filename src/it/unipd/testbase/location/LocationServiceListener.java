package it.unipd.testbase.location;

import android.location.Location;

/**
 * listener to register in LocationService to obtain location updates
 * 
 * @author Fabio De Gaspari
 *
 */
public interface LocationServiceListener {
	void onLocationChanged(Location location);
}
