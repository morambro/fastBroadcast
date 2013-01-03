package it.unipd.testbase.eventdispatcher;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.helper.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements an Event Bus, at which classes implementing IComponent can register
 * in order to declare events of interest, and to receive them when triggered.
 * 
 * @author Fabio De Gaspari
 *
 */
public class EventDispatcher {
	protected final String TAG = "it.unipd.testbase";

	private static EventDispatcher singleton = null;
	private Map<IComponent, List<Class<? extends IEvent>>> components = new HashMap<IComponent, List<Class<? extends IEvent>>>();

	public static EventDispatcher getInstance() {
		if(singleton==null)
			singleton = new EventDispatcher();
		return singleton;
	}

	protected EventDispatcher(){}

	/**
	 * Method called to register a component, to listen for certain event types
	 * 
	 * @param component
	 * @param events
	 */
	public synchronized void registerComponent(IComponent component, List<Class<? extends IEvent>> events) {
		Log.e(TAG, "*******************************************"+component.getClass().getSimpleName()+" now registered");
		if(components.containsKey(component)) {
			Log.e(TAG, this.getClass().getSimpleName()+": Component "+component.getClass().getSimpleName()+" already registered");
			return;
		}
		components.put(component, events);
	}
	
	public synchronized void unregisterComponent(IComponent component) {
		components.remove(component);
	}

	/**
	 * Method used to obtain a component reference.
	 * 
	 * @param component
	 * @return
	 */
	public IComponent requestComponent(Class<? extends IComponent> component) {
		for (IComponent comp : components.keySet()) {
			if(component.isInstance(comp))
				return comp;
		}
		return null;
	}

	/**
	 * Method used to fire an event and find the correct component
	 * 
	 * @param event
	 * @return
	 */
	public synchronized boolean triggerEvent(IEvent event) {
		boolean flag = false;
		//Log.d(TAG,this.getClass().getSimpleName()+" : Triggered event "+event.getClass().getSimpleName());
		for(IComponent comp : components.keySet()) {
			if(components.get(comp) != null && components.get(comp).contains(event.getClass())) {
				flag = true;
				comp.handle(event);
			}
		}
		return flag;
	}
}
