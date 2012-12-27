package it.unipd.testbase.eventdispatcher.event.protocol;

import it.unipd.testbase.eventdispatcher.event.IEvent;

public class StopSimulationEvent implements IEvent {
	public boolean showResults = false;
	
	public StopSimulationEvent(boolean showResults) {
		this.showResults = showResults;
	}
}
