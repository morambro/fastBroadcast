package it.unipd.testbase.protocol;

import it.unipd.testbase.eventdispatcher.IComponent;

/**
 * Range Estimator interface. 
 * 
 * @author Moreno Ambrosin
 *
 */
public interface IFastBroadcastComponent extends IComponent{
	
	/**
	 * Provides an estimation of Transmission Range
	 * 
	 * @return
	 */
	public double getEstimatedTrasmissionRange();
	
	/**
	 * Stops the execution
	 */
	public void stopExecuting();
	
	
}