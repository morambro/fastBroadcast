package it.unipd.fast.broadcast.location;

import it.unipd.fast.broadcast.IComponent;

/**
 * Interface returned on service creation
 * 
 * @author Fabio De Gaspari
 *
 */
public interface ILocationComponent extends IComponent {
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
}
