package it.unipd.vanets.framework.eventdispatcher.event.protocol;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;

public class StopSimulationEvent implements IEvent {
	public boolean showResults = false;
	public boolean quitSimulation = false;
	
	public StopSimulationEvent(boolean showResults,boolean quitSimulation) {
		this.showResults = showResults;
		this.quitSimulation = quitSimulation;
	}
}
