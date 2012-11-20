package it.unipd.fast.broadcast.wifi_connection.receiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * The receiver used to receive and manage intents
 * 
 * @author Moreno Ambrosin
 *
 */
public class FastBroadcastReceiver extends BroadcastReceiver {
	protected final String TAG = "it.unipd.fast.broadcast";

	private WifiP2pManager manager;
    private Channel channel;
    private PeerListListener peer_listener;
	private ConnectionInfoListener connection_info_listener;
	
    /**
     * Class Used to listen to Wi-fi channel events, and handles connection creation
     * 
     * @param manager
     * @param channel
     * @param activity
     */
    public FastBroadcastReceiver(
    		WifiP2pManager manager,
    		Channel channel,
    		PeerListListener peer_listener,
    		ConnectionInfoListener connection_info_listener) {
    	this.manager = manager;
    	this.channel = channel;
    	this.peer_listener = peer_listener;
    	this.connection_info_listener = connection_info_listener;
    }
    
	@Override
	public void onReceive(final Context context, Intent intent) {
		
		// This method is invoked by the system when a specific event occours
		String action = intent.getAction();
		
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			Log.d(TAG, this.getClass().getSimpleName()+": onReceive called with action WIFI_P2P_STATE_CHANGED_ACTION");
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
	        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	        	Log.d(TAG, this.getClass().getSimpleName()+": All OK, Wi-Fi Direct is enabled");
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
			Log.d(TAG, this.getClass().getSimpleName()+": Peers Received on WIFI_P2P_PEERS_CHANGED_ACTION");
        	if (manager != null) {
        		Log.d(TAG, this.getClass().getSimpleName()+": Requesting peers...");
        		manager.requestPeers(channel, peer_listener);
            }else Log.d(TAG, this.getClass().getSimpleName()+": manager is null!");
        	
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        	Log.d(TAG, this.getClass().getSimpleName()+": onReceive called with action WIFI_P2P_CONNECTION_CHANGED_ACTION");
        	if (manager == null) {
        		Log.d(TAG, this.getClass().getSimpleName()+": manager is null!");
                return;
            }
        	
        	// Extract network Info from the fired Intent
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
            	Toast.makeText(context, "We are connected to a Group", Toast.LENGTH_LONG).show();
                // We are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, connection_info_listener);
            }else{
            	Log.d(TAG, this.getClass().getSimpleName()+": Not connected anymore");
            }
        	
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        	Log.d(TAG, this.getClass().getSimpleName()+": onReceive called with action WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        	manager.discoverPeers(channel, null);
        }
	}

}
