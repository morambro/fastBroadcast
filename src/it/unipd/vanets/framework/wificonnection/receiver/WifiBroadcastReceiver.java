package it.unipd.vanets.framework.wificonnection.receiver;

import it.unipd.vanets.framework.helper.DebugLogger;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.provider.Settings;
import android.widget.Toast;

/**
 * The receiver used to receive and manage intents
 * 
 * @author Moreno Ambrosin
 *
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {
	protected final String TAG = "it.unipd.vanets.framework";
	
	DebugLogger logger = new DebugLogger(WifiBroadcastReceiver.class);

	private WifiP2pManager manager;
    private Channel channel;
    private PeerListListener peerListener;
	private ConnectionInfoListener connectionInfoListener;
	
    /**
     * Class Used to listen to Wi-fi channel events, and handles connection creation
     * 
     * @param manager
     * @param channel
     * @param activity
     */
    public WifiBroadcastReceiver(
    		WifiP2pManager manager,
    		Channel channel,
    		PeerListListener peerListener,
    		ConnectionInfoListener connectionInfoListener) {
    	this.manager = manager;
    	this.channel = channel;
    	this.peerListener = peerListener;
    	this.connectionInfoListener = connectionInfoListener;
    }
    
	@Override
	public void onReceive(final Context context, Intent intent) {
		
		// This method is invoked by the system when a specific event occours
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	        	logger.d("All OK, Wi-Fi Direct is enabled");
	        	
	        	// If Wifi is enabled, launch peers discovering
	        	manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
	    			public void onSuccess() {
	    				logger.d("Discover Peers onSuccess called");
	    			}

	    			public void onFailure(int reasonCode) {
	    				logger.d("Discover Peers ERROR: "+reasonCode);
	    			}
	    		});
	        
	        } else {
	            // Wi-Fi Direct is not enabled, show Wireless settings
	        	AlertDialog.Builder alert = new AlertDialog.Builder(context);

	            alert.setMessage("Wifi direct is not enabled, please turn on the Wi-fi connection and Wi-fi direct.");
	            alert.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
	                
	            	@Override
	                public void onClick(DialogInterface dialog, int arg1){
	                	context.startActivity(new Intent(Settings.ACTION_SETTINGS));
	                    dialog.cancel();
	                }
	            	
	            });
	            
	            alert.show();
	        	
	        }
        
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        	if (manager != null) {
        		logger.d("Requesting peers...");
        		manager.requestPeers(channel, peerListener);
            }else {
            	logger.d("manager is null!");
            }
        	
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        	if (manager == null) {
        		logger.d("manager is null!");
                return;
            }
        	
        	// Extract network Info from the fired Intent
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
            	Toast.makeText(context, "We are conencted to a Group", Toast.LENGTH_LONG).show();
                // We are requestConnectionSent with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, connectionInfoListener);
            }else{
            	logger.d("Not requestConnectionSent anymore");
            }
        	
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        	manager.discoverPeers(channel, null);
        }
	}

}
