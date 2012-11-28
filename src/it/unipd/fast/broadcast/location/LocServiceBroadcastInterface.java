package it.unipd.fast.broadcast.location;

import android.location.Location;

/**
 * Interface returned on service creation
 * 
 * @author Fabio De Gaspari
 *
 */
public interface LocServiceBroadcastInterface {
	/**
	 * Adds a location listener 
	 * 
	 * @param listener
	 */
	void addLocationListener(LocationServiceListener listener);
	/**
	 * Removes the specified location service listener
	 * 
	 * @param listener
	 */
	void removeLocationListener(LocationServiceListener listener);
	/**
	 * Returns the last known location
	 * 
	 * @return
	 */
	Location getLastLocation();
	MockLocationProvider __get_mock_provider();
}
