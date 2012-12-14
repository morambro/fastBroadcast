package it.unipd.fast.broadcast.wificonnection.connectionmanager;

import it.unipd.fast.broadcast.AppController;
import it.unipd.fast.broadcast.EventDispatcher;
import it.unipd.fast.broadcast.ProceedWithNextEvent;
import it.unipd.fast.broadcast.event.connectioninfo.WiFiInfoCollectedEvent;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;
import it.unipd.fast.broadcast.wificonnection.message.MessageBuilder;
import it.unipd.fast.broadcast.wificonnection.transmissionmanager.TransmissionManagerFactory;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;


/**
 * ConnectionInfoListener implementation, used to react on connection available
 */
public class ConnectionInfoManager implements IConnectionInfoManager{

	protected final String TAG = "it.unipd.fast.broadcast";

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		// Create a Thread to execute potentially blocking operations.
		new Thread(){
			public void run(){
				// group owner InetAddress from WifiP2pInfo.
				String groupOwnerAddress = info.groupOwnerAddress.getCanonicalHostName();

				// After the group negotiation, we can determine the group owner.
				Log.d(TAG, this.getClass().getSimpleName()+": GroupFormed = "+info.groupFormed);
				Log.d(TAG, this.getClass().getSimpleName()+": isGroupOwner = "+info.isGroupOwner);
				Log.d(TAG, this.getClass().getSimpleName()+": Address = "+groupOwnerAddress);

				// Check if the group is formed
				if (info.groupFormed){
					
					// Fire an Event to pass info to Controller
					EventDispatcher.getInstance().triggerEvent(new WiFiInfoCollectedEvent(info));
					
					// Depending on being the group owner or not, there are different tasks
					// to do at this point
					if(info.isGroupOwner){
						// Do nothing, simply waits for other devices to send Hello messages
						Log.d(TAG, this.getClass().getSimpleName()+": I'am the group owner");

					}else{
						// Not group Owner, so send an Hello Message to the GroupOwner
						final WifiP2pInfo info2 = info;
						Log.d(TAG, ConnectionInfoManager.class.getSimpleName()+": Sending info!!");

						new Thread(){
							public void run() {
								try{
									String groupOwnerAddress = info2.groupOwnerAddress.getCanonicalHostName();
									IMessage message = MessageBuilder.getInstance().getMessage(
											IMessage.PING_MESSAGE_TYPE, 
											groupOwnerAddress,
											IMessage.PING_MESSAGE_ID_KEY, 
											AppController.MAC_ADDRESS);
									Log.d(TAG, ConnectionInfoManager.class.getSimpleName()+": Sending my address to Group owner");
									TransmissionManagerFactory.getInstance().getTransmissionManager().send(
											groupOwnerAddress,	// GroupOwner IP
											message); //PING message
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
	}

}
