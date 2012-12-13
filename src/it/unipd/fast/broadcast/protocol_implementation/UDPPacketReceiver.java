package it.unipd.fast.broadcast.protocol_implementation;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.receiver.IDataReceiverService.IDataCollectionHandler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.util.Log;

public class UDPPacketReceiver extends AbstractPacketReceiver {
	protected final String TAG = "it.unipd.fast.broadcast";
	
	protected final int PACKET_SIZE = 1024;
	protected int server_port = 8888;
	
	protected DatagramSocket socket;
	
	@Override
	public void run() {
		try{
			socket = new DatagramSocket(server_port);
			final byte[] message = new byte[PACKET_SIZE];
			final DatagramPacket p = new DatagramPacket(message, message.length);
			while(!terminated){
				
				socket.receive(p);
				new Thread(){
					public void run(){
						String xmlMsg = new String(message, 0, p.getLength());
						IMessage xmlMessage = MessageBuilder.getInstance().getMessage(xmlMsg);
						
						Log.d(TAG,UDPPacketReceiver.class.getSimpleName() +"message : " + xmlMsg);
						Log.d(TAG,UDPPacketReceiver.class.getSimpleName() +"handlers : " + handlers);
						
						for (IDataCollectionHandler handler : handlers) {
							Log.d(TAG,"Calling handler ");
							handler.onDataCollected(
								xmlMessage ,
								xmlMessage .getContent().get(IMessage.SENDER_IP_ADDR)
							);
						}
					}
				}.start();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			disconnectSocket();
		}
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
