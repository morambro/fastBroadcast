package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.IComponent;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.SetMessageFilterEvent;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.transmissionmanager.sender.IPaketSender;

import java.util.ArrayList;
import java.util.List;

public class TransmissionManager implements IComponent{
	
	private static TransmissionManager instance =  new TransmissionManager();
	
	public static TransmissionManager getInstance(){
		return instance;
	}
	
	public interface TransportSelectorFilter{
		int getTransportForMessage(IMessage message);
	}
	
	private TransportSelectorFilter filter = null;
	
	public TransmissionManager() {
		register();
	}
	
	public void sendUnicast(String ID,IMessage message){
		int transportType = PacketSenderFactory.RELIABLE_TRANSPORT;
		if(filter != null){
			transportType = filter.getTransportForMessage(message);
		}
		IPaketSender m = PacketSenderFactory.getInstance().getTransmissionManager(transportType);
		m.send(ID, message);
	}
	
	public void sendBroadcast(List<String> IDs,IMessage message){
		int transportType = PacketSenderFactory.RELIABLE_TRANSPORT;
		if(filter != null){
			transportType = filter.getTransportForMessage(message);
		}
		IPaketSender m = PacketSenderFactory.getInstance().getTransmissionManager(transportType);
		m.send(IDs, message);
	}


	public void setFilter(TransportSelectorFilter filter){
		this.filter = filter;
	}
	
	@Override
	public void handle(IEvent event) {
		if(event instanceof SetMessageFilterEvent){
			SetMessageFilterEvent ev = (SetMessageFilterEvent) event;
			this.filter = ev.filter;
			return;
		}
	}

	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(SetMessageFilterEvent.class);
		EventDispatcher.getInstance().registerComponent(this, events);
	}
}
