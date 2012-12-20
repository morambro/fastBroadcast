package it.unipd.testbase.wificonnection.transmissionmanager;


public class TransmissionManagerFactory {
	
	public static final int RELIABLE_TRANSPORT 		= 0;
	
	public static final int UNRELIABLE_TRANSPORT 	= 1;
	
	private static final TransmissionManagerFactory singleton = new TransmissionManagerFactory();
	
	private TransmissionManagerFactory() {}
	
	public static TransmissionManagerFactory getInstance() {
		return singleton;
	}
	
	public ITranmissionManager getTransmissionManager(int transportTypes) {
		switch(transportTypes){
			case RELIABLE_TRANSPORT 	: return new TCPTransmissionManager();
			case UNRELIABLE_TRANSPORT 	: return new UDPTransmissionManager();
		}
		return null;
	}
}
