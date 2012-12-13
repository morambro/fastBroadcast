package it.unipd.fast.broadcast;

import java.util.List;
import java.util.Map;


public class EventDispatcher {
	
	private static EventDispatcher singleton = null;
	private Map<IComponent, List<Class<? extends IEvent>>> components;
	
	public static EventDispatcher getInstance() {
		if(singleton==null)
			singleton = new EventDispatcher();
		return singleton;
	}
	
	protected EventDispatcher()
	{
	}
	
	public void registerComponent(IComponent component, List<Class<? extends IEvent>> events) {
		components.put(component, events);
	}
	
	public IComponent requestComponent(Class component) {
		for (IComponent comp : components.keySet()) {
			if(component.isInstance(comp))
				return comp;
		}
		return null;
	}
	
	public boolean triggerEvent(IEvent event) {
		boolean flag = false;
		for(IComponent comp : components.keySet()) {
			for (Class<? extends IEvent> evClass : components.get(comp)) {
				if(evClass.isInstance(event)) {
					flag = true;
					comp.handle(event);
				}
			}
		}
		return flag;
	}
}
