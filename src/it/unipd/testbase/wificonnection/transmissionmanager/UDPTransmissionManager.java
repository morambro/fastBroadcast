package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import android.util.Log;

public class UDPTransmissionManager implements ITranmissionManager{
	private static final String TAG = "it.unipd.testbase";
	
	@Override
	public void send(final String ID,final IMessage msg) {

		new Thread(){
			public void run(){
				try {
					DatagramSocket socket = new DatagramSocket();
					
					// Including IP address in Message attribute
					IMessage newMessage = MessageBuilder.getInstance().getMessage(
							msg.getType(), 
							msg.getRecipientAddress()
					);
					for(String contentKey : msg.getContent().keySet()){
						newMessage.addContent(contentKey, msg.getContent().get(contentKey));
					}
					newMessage.prepare();
					int messageLength = newMessage.getMessage().length;
					Log.d(TAG,UDPTransmissionManager.class.getSimpleName()+" : Message Length (bytes)= "+messageLength);
					
					DatagramPacket p = new DatagramPacket(
							newMessage.getMessage(), 
							messageLength,
							InetAddress.getByName(ID),
							UDP_PORT);
					
					socket.send(p);
				} catch (Exception e) {
					Log.d(TAG, UDPTransmissionManager.class.getSimpleName()+": "+e.getMessage());
				}
			}
		}.start();	}

	@Override
	public void send(List<String> IDs, IMessage msg) {
//		for(String ID : IDs){
//			send(ID,msg);
//		}
		send(ITranmissionManager.BROADCAST_ADDRESS,msg);
	}
	
}
