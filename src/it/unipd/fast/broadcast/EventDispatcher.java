package it.unipd.fast.broadcast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;


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
	
	public void registerComponent(IComponent component, List<Class<? extends IEvent>> events) {
		components.put(component, events);
	}
	
	public IComponent requestComponent(Class<? extends IComponent> component) {
		for (IComponent comp : components.keySet()) {
			if(component.isInstance(comp))
				return comp;
		}
		return null;
	}
	
	public boolean triggerEvent(IEvent event) {
		boolean flag = false;
		Log.d(TAG,this.getClass().getSimpleName()+" : event "+event.getClass().getSimpleName());
		for(IComponent comp : components.keySet()) {
			if(components.get(comp) != null) 
				for (Class<? extends IEvent> evClass : components.get(comp)) {
					if(evClass.isInstance(event)) {
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
