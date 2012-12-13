package it.unipd.fast.broadcast.protocol;

import it.unipd.fast.broadcast.IComponent;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;
import android.location.Location;

/**
 * Range Estimator interface. 
 * 
 * @author Moreno Ambrosin
 *
 */
public interface IFastBroadcastComponent extends IComponent{
	
	public double getEstimatedRange();
	
	/**
	 * Stops the execution
	 */
	public void stopExecuting();
	
	/**
	 * Handles an incoming generic message
	 * 
	 * @param message
	 */
	public void handleMessage(IMessage message);

	/**
	 * Setter method used by controller to pass location to the comminication handler
	 * 
	 * @param location
	 */
	public void setCurrentLocation(Location location);
	
}