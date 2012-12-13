package it.unipd.fast.broadcast.wifi_connection.transmissionmanager;


public class TransmissionManagerFactory {
	private static final TransmissionManagerFactory singleton = new TransmissionManagerFactory();
	
	private TransmissionManagerFactory() {}
	
	public static TransmissionManagerFactory getInstance() {
		return singleton;
	}
	
	public ITranmissionManager getTransmissionManager() {
		return new TCPTransmissionManager();
	}
}
