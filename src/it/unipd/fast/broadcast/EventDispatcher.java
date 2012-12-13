package it.unipd.fast.broadcast;

public class EventDispatcher {
	
	private static EventDispatcher singleton = null;
	
	public static EventDispatcher getInstance() {
		if(singleton==null)
			singleton = new EventDispatcher();
		return singleton;
	}
	
	public registerListener() {
		
	}
}
