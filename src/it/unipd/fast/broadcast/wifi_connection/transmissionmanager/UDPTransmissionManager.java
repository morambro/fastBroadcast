package it.unipd.fast.broadcast.wifi_connection.transmissionmanager;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;

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
	private static final String TAG = "it.unipd.fast.broadcast";
	private static final int port = 8888;
	
	@Override
	public void send(final String ID,final IMessage msg) {

		new Thread(){
			public void run(){
				try {
					DatagramSocket socket = new DatagramSocket();
					/**
					 * Create a client socket with the ID,
					 * port, and timeout information.
					 */
					IMessage newMessage = MessageBuilder.getInstance().getMessage(msg.getType(), msg.getRecipientAddress());
					for(String contentKey : msg.getContent().keySet()){
						newMessage.addContent(contentKey, msg.getContent().get(contentKey));
					}
					newMessage.addContent(IMessage.SENDER_IP_ADDR, getLocalIPAddress().getCanonicalHostName());
					newMessage.prepare();
					
					int messageLength = newMessage.getMessage().length;
					Log.d(TAG,UDPTransmissionManager.class.getSimpleName()+" : Message Length (bytes)= "+messageLength);
					
					DatagramPacket p = new DatagramPacket(newMessage.getMessage(), messageLength,InetAddress.getByName(ID),port);
					
					socket.send(p);
				} catch (Exception e) {
					Log.d(TAG, UDPTransmissionManager.class.getSimpleName()+": "+e.getMessage());
				}
			}
		}.start();	}

	@Override
	public void send(List<String> IDs, IMessage msg) {
		for(String ID : IDs){
			send(ID,msg);
		}
	}
	
	 private static InetAddress getLocalIPAddress() { 
	        try { 
	            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
	                NetworkInterface intf = en.nextElement(); 
	                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
	                    InetAddress inetAddress = enumIpAddr.nextElement(); 
	                    if (!inetAddress.isLoopbackAddress()) { 
	                        if (inetAddress instanceof Inet4Address && !inetAddress.getCanonicalHostName().endsWith(".lan")) { 
	                        	// fix for Galaxy Nexus. IPv4 is easy to use :-) 
	                        	return inetAddress;
	                        }
	                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6 
	                    } 
	                } 
	            } 
	        } catch (SocketException ex) { 
	            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex); 
	        } catch (NullPointerException ex) { 
	            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex); 
	        } 
	        return null; 
	    } 
}
