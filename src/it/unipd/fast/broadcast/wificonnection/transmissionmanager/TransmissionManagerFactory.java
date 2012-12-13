package it.unipd.fast.broadcast.wificonnection.transmissionmanager;


public class TransmissionManagerFactory {
	private static final TransmissionManagerFactory singleton = new TransmissionManagerFactory();
	
	private TransmissionManagerFactory() {}
	
	public static TransmissionManagerFactory getInstance() {
		return singleton;
	}
	
	public ITranmissionManager getTransmissionManager() {
		return new UDPTransmissionManager();
	}
}
