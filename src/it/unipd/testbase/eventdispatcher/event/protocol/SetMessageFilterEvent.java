package it.unipd.testbase.eventdispatcher.event.protocol;

import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.wificonnection.transmissionmanager.TransmissionManager.TransportSelectorFilter;

public class SetMessageFilterEvent implements IEvent {
	public TransportSelectorFilter filter;
	
	public SetMessageFilterEvent(TransportSelectorFilter filter) {
		this.filter = filter;
	}
}
