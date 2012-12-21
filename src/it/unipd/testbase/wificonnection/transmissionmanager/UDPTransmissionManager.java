package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.wificonnection.message.IMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import android.util.Log;

public class UDPTransmissionManager implements ITranmissionManager{
	private static final String TAG = "it.unipd.testbase";
	
	@Override
	public void send(final String ID,final IMessage message) {

		new Thread(){
			public void run(){
				try {
					DatagramSocket socket = new DatagramSocket();
					
					int messageLength = message.getMessage().length;
					Log.d(TAG,UDPTransmissionManager.class.getSimpleName()+" : Message Length (bytes)= "+messageLength);
					
					DatagramPacket p = new DatagramPacket(
							message.getMessage(), 
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
		send(ITranmissionManager.BROADCAST_ADDRESS,msg);
	}
	
}
