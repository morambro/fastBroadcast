package it.unipd.fast.broadcast.protocol_implementation;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import android.location.Location;

/**
 * Range Estimator interface. 
 * 
 * @author Moreno Ambrosin
 *
 */
public interface ICommunicationHandler {

	/**
	 * Interface used to specify actions to do after the message
	 * is forwarded
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public interface OnForwardedHandler {
		void doOnForwarded();
	}
	
	/**
	 * Turn duration in milliseconds
	 */
	public static final int TURN_DURATION = 20000;
	
	public double getEstimatedRange();
	
	/**
	 * Method used to tell whether a hello message already arrived
	 * 
	 * @param arrived
	 */
	public void setHelloMessageArrived(boolean arrived);
	
	/**
	 * Method used to pass an Hello Message to the estimation object
	 * 
	 * @param message
	 */
	public void helloMessageReceived(IMessage message);

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
	
	/**
	 * 
	 * @param handler
	 */
	public void setOnforwardedHadler(OnForwardedHandler handler);
	
}