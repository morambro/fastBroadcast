package it.unipd.testbase.wificonnection.receiver;


import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.helper.DebugLogger;
import it.unipd.testbase.wificonnection.receiver.transport.TCPPacketReceiver;
import it.unipd.testbase.wificonnection.receiver.transport.UDPPacketReceiver;


/**
 * Service used to receive data from other requestConnectionSent devices. It creates a Socket and waits 
 * for incoming connections.
 * 
 * @author Moreno Ambrosin
 *
 */
public class DataReceiverService implements IDataReceiverComponent{
	protected final String TAG = "it.unipd.testbase";

	private DebugLogger logger = new DebugLogger(DataReceiverService.class);
	private AbstractPacketReceiver udpPacketReceiver;
	private AbstractPacketReceiver tcpPacketReceiver;
	
	private static DataReceiverService instance = null;
	
	public static DataReceiverService getInstance() {
		if(instance == null)
			instance = new DataReceiverService();
		return instance;
	}
	
	public void stopExecuting() {
		udpPacketReceiver.terminate();
		tcpPacketReceiver.terminate();
	}

	protected DataReceiverService() {
		logger.d("Servizio creato");
		udpPacketReceiver = new UDPPacketReceiver();
		tcpPacketReceiver = new TCPPacketReceiver();
		udpPacketReceiver.start();
		tcpPacketReceiver.start();
	}

	@Override
	public void handle(IEvent event) {}

	@Override
	public void register() {}	
}
