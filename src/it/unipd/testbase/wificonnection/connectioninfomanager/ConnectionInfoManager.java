package it.unipd.testbase.wificonnection.connectioninfomanager;

import it.unipd.testbase.AppController;
import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.connectioninfo.WiFiInfoCollectedEvent;
import it.unipd.testbase.eventdispatcher.event.message.SendUnicastMessageEvent;
import it.unipd.testbase.helper.DebugLogger;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;


/**
 * ConnectionInfoListener implementation, used to react on connection available
 */
public class ConnectionInfoManager implements IConnectionInfoManager{

	protected final String TAG = "it.unipd.testbase";
	private DebugLogger logger = new DebugLogger(ConnectionInfoManager.class);
	boolean __ping_sent = false;

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		// Create a Thread to execute potentially blocking operations.
		new Thread(){
			public void run(){
				// group owner InetAddress from WifiP2pInfo.
				String groupOwnerAddress = info.groupOwnerAddress.getCanonicalHostName();

				// After the group negotiation, we can determine the group owner.
				logger.d("GroupFormed = "+info.groupFormed);
				logger.d("isGroupOwner = "+info.isGroupOwner);
				logger.d("Address = "+groupOwnerAddress);

				// Check if the group is formed
				if (info.groupFormed){
					
					// Fire an Event to pass info to Controller
					EventDispatcher.getInstance().triggerEvent(new WiFiInfoCollectedEvent(info));
					
					// Depending on being the group owner or not, there are different tasks
					// to do at this point
					if(info.isGroupOwner){
						// Do nothing, simply waits for other devices to send Hello messages
						logger.d("I'am the group owner");

					}else{
						// Not group Owner, so send an Hello Message to the GroupOwner
						logger.d("Sending info!!");

						if(__ping_sent == false){
							new Thread(){
								public void run() {
									try{
										String groupOwnerAddress = info.groupOwnerAddress.getCanonicalHostName();
										IMessage message = MessageBuilder.getInstance().getMessage(
												IMessage.PING_MESSAGE_TYPE, 
												groupOwnerAddress,
												IMessage.PING_MESSAGE_ID_KEY, 
												AppController.MAC_ADDRESS);
										Log.d(TAG, ConnectionInfoManager.class.getSimpleName()+": Sending my address to Group owner");
										EventDispatcher.getInstance().triggerEvent(
												new SendUnicastMessageEvent(message, groupOwnerAddress));
									}catch(Exception e){
										logger.e(e);
									}
								};
							}.start();
							__ping_sent = true;
						} else {
							logger.d("Second attempt to send PING");
						}

						logger.d("I am NOT the group owner");
					}
				}
			}
		}.start();
	}

}
