package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.event.IEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * This class implements an Event Bus, at which classes implementing IComponent can register
 * in order to declare events of interest, and to receive them when triggered.
 * 
 * @author Fabio De Gaspari
 *
 */
public class EventDispatcher {
	protected final String TAG = "it.unipd.fast.broadcast";
	
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
	public void registerComponent(IComponent component, List<Class<? extends IEvent>> events) {
		components.put(component, events);
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
	public boolean triggerEvent(IEvent event) {
		boolean flag = false;
		Log.d(TAG,this.getClass().getSimpleName()+" : event "+event.getClass().getSimpleName());
		for(IComponent comp : components.keySet()) {
			if(components.get(comp) != null) 
				for (Class<? extends IEvent> evClass : components.get(comp)) {
					if(evClass.equals(event.getClass())) {
						Log.d(TAG,this.getClass().getSimpleName()+" : event match "+evClass.getSimpleName() +"" +
								" handled by "+comp.getClass().getSimpleName());
						flag = true;
						comp.handle(event);
						break;
					}
				}
		}
		return flag;
	}
}
