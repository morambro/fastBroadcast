package it.unipd.testbase.wificonnection.receiver.protocols;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.message.MessageReceivedEvent;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;
import it.unipd.testbase.wificonnection.receiver.AbstractPacketReceiver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.util.Log;

public class UDPPacketReceiver extends AbstractPacketReceiver {
	protected final String TAG = "it.unipd.testbase";
	
	protected final int PACKET_SIZE = 600;
	
	protected DatagramSocket socket;
	
	@Override
	public void run() {
		try{
			socket = new DatagramSocket(UDP_PORT);
			while(!terminated){
				final byte[] message = new byte[PACKET_SIZE];
				final DatagramPacket p = new DatagramPacket(message, message.length);
				socket.receive(p);
				new Thread(){
					public void run(){
						
						String xmlMsg = new String(message, 0, p.getLength());
						IMessage xmlMessage = MessageBuilder.getInstance().getMessage(xmlMsg);
						
						Log.d(TAG,UDPPacketReceiver.class.getSimpleName() +"message : " + xmlMsg);
						
						EventDispatcher.getInstance().triggerEvent(new MessageReceivedEvent(
								xmlMessage ,
								p.getAddress().getCanonicalHostName()
						));
					}
				}.start();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		disconnectSocket();
	}

	@Override
	public void terminate() {
		this.terminated = false;
	}
	
	/**
	 * Method called to close the opened TCP socket if different to null and opened
	 * 
	 */
	private void disconnectSocket(){
		if(socket != null && !socket.isClosed()){
			try{
				socket.close();
				Log.d(TAG, this.getClass().getSimpleName()+": Ho chiuso il socket");
			}catch(Exception e){
				Log.d(TAG, this.getClass().getSimpleName()+": Impossibile chiudere il socket");
			}
		}
	}

}
