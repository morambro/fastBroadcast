package it.unipd.fast.broadcast.wifi_connection.connectionmanager;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.net.InetAddress;

import android.net.wifi.p2p.WifiP2pInfo;
import android.provider.Settings;
import android.util.Log;


/**
 * ConnectionInfoListener implementation, used to react on connection available
 */
public class ConnectionInfoManager implements IConnectionInfoManager {
	protected final String TAG = "it.unipd.fast.broadcast";


	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		// Create a Thread to execute potentially blocking operations.
		new Thread(){
			public void run(){
				// group owner InetAddress from WifiP2pInfo.
				InetAddress groupOwnerAddress = info.groupOwnerAddress;

				// After the group negotiation, we can determine the group owner.
				Log.d(TAG, this.getClass().getSimpleName()+": GroupFormed = "+info.groupFormed);
				Log.d(TAG, this.getClass().getSimpleName()+": isGroupOwner = "+info.isGroupOwner);
				Log.d(TAG, this.getClass().getSimpleName()+": Address = "+groupOwnerAddress.getCanonicalHostName());

				// Check if the group is formed
				if (info.groupFormed){
					// Depending on being the group owner or not, there are different tasks
					// to do at this point
					if(info.isGroupOwner){
						// Do nothing, simply waits for other devices to send Hello messages
						Log.d(TAG, this.getClass().getSimpleName()+": I'am the group owner");

						// Waits 10 sec for the handshake, after it send map to all
//						new Timer().schedule(new TimerTask() {
//
//							@Override
//							public void run() {
//								try{
//									Log.d(TAG, this.getClass().getSimpleName()+": Sending map to all!!");
//									IMessage message = MessageBuilder.getInstance().getMessage(IMessage.CLIENT_MAP_MESSAGE_TYPE, "doesn't matter");
//									for(String key : peer_id_ip_map.keySet())
//										message.addContent(key, peer_id_ip_map.get(key));
//									for(String key : peer_id_ip_map.keySet()){
//										message.setRecipientAddress(key);
//										TransmissionManagerFactory.getInstance().getTransmissionManager().send(
//												peer_id_ip_map.get(key),
//												message);
//									}
//								}catch(Exception e){
//
//								}
//							}
//						},10000);

					}else{
						// Not group Owner, so send an Hello Message to the GroupOwner
						final WifiP2pInfo info2 = info;
						Log.d(TAG, this.getClass().getSimpleName()+": Sending info!!");

						new Thread(){
							public void run() {
								try{
									Log.d(TAG, this.getClass().getSimpleName()+": Sending my address to Group owner");
									String groupOwnerAddress = info2.groupOwnerAddress.getCanonicalHostName();
									TransmissionManagerFactory.getInstance().getTransmissionManager().send(
											groupOwnerAddress,	// GroupOwner IP
											MessageBuilder.getInstance().getMessage(
													IMessage.PING_MESSAGE_TYPE, 
													groupOwnerAddress,
													IMessage.PING_MESSAGE_ID_KEY, 
													Settings.Secure.ANDROID_ID)); //PING message
								}catch(Exception e){
									e.printStackTrace();
								}
							};
						}.start();

						Log.d(TAG, this.getClass().getSimpleName()+": I am NOT the group owner");
					}
				}
			}
		}.start();
	};

}
