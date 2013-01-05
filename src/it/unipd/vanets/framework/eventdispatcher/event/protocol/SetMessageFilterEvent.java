package it.unipd.vanets.framework.eventdispatcher.event.protocol;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.wificonnection.transmissionmanager.TransmissionManager.TransportSelectorFilter;

public class SetMessageFilterEvent implements IEvent {
	public TransportSelectorFilter filter;
	
	public SetMessageFilterEvent(TransportSelectorFilter filter) {
		this.filter = filter;
	}
}
