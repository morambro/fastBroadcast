package it.unipd.vanets.framework.wificonnection.transmissionmanager;

import it.unipd.vanets.framework.wificonnection.transmissionmanager.sender.IPaketSender;
import it.unipd.vanets.framework.wificonnection.transmissionmanager.sender.TCPPacketSender;
import it.unipd.vanets.framework.wificonnection.transmissionmanager.sender.UDPPacketSender;


public class PacketSenderFactory {
	
	public static final int RELIABLE_TRANSPORT 		= 0;
	
	public static final int UNRELIABLE_TRANSPORT 	= 1;
	
	private static final PacketSenderFactory singleton = new PacketSenderFactory();
	
	private PacketSenderFactory() {}
	
	public static PacketSenderFactory getInstance() {
		return singleton;
	}
	
	public IPaketSender getTransmissionManager(int transportTypes) {
		switch(transportTypes){
			case RELIABLE_TRANSPORT 	: return new TCPPacketSender();
			case UNRELIABLE_TRANSPORT 	: return new UDPPacketSender();
		}
		return null;
	}
}
