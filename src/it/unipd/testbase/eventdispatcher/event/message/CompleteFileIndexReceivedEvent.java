package it.unipd.testbase.eventdispatcher.event.message;

import it.unipd.testbase.eventdispatcher.event.IEvent;

import java.util.HashMap;
import java.util.Map;

public class CompleteFileIndexReceivedEvent implements IEvent {
	public Map<String, String> map = new HashMap<String, String>();
	
	public CompleteFileIndexReceivedEvent(Map<String, String> m) {
		map = m;
	}
	
	public void addIndex(String key, String value) {
		map.put(key, value);
	}
	
	public String getIndex(String key) {
		return map.get(key);
	}
}
