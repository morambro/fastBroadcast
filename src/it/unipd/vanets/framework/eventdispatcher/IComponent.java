package it.unipd.vanets.framework.eventdispatcher;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;

/**
 * Interface used to register to the Event Bus, to receive events
 * 
 * @author Fabio De Gaspari
 *
 */
public interface IComponent {
	/**
	 * Express operation to do when a triggered event arrives
	 * 
	 * @param event
	 */
	public void handle(IEvent event);
	/**
	 * Contains operations to be done when registering to the Event Bus
	 * 
	 */
	public void register();
}
