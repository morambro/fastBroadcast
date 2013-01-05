package it.unipd.vanets.framework.wificonnection.transmissionmanager.sender;

import it.unipd.vanets.framework.helper.DebugLogger;
import it.unipd.vanets.framework.wificonnection.message.IMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class UDPPacketSender implements IPaketSender{
	
	private DebugLogger logger = new DebugLogger(UDPPacketSender.class);
	
	protected static final String TAG = "it.unipd.vanets.framework";
	
	@Override
	public void send(final String ID,final IMessage message) {

		new Thread(){
			public void run(){
				try {
					DatagramSocket socket = new DatagramSocket();
					
					int messageLength = message.getMessage().length;
					logger.d("Message Length (bytes)= "+messageLength);
					
					DatagramPacket p = new DatagramPacket(
							message.getMessage(), 
							messageLength,
							InetAddress.getByName(ID),
							UDP_PORT);
					
					socket.send(p);
				} catch (Exception e) {
					logger.d(e.getMessage());
				}
			}
		}.start();	}

	@Override
	public void send(List<String> IDs, IMessage msg) {
		send(IPaketSender.BROADCAST_ADDRESS,msg);
	}
	
}
