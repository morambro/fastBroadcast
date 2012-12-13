package it.unipd.fast.broadcast.wifi_connection.connectionmanager;

import android.net.wifi.p2p.WifiP2pInfo;
import it.unipd.fast.broadcast.IEvent;

public class WiFiInfoCollectedEvent implements IEvent {
	
	public WifiP2pInfo wifiConnectionInfo;
	
	public WiFiInfoCollectedEvent(WifiP2pInfo wifiConnectionInfo){
		this.wifiConnectionInfo = wifiConnectionInfo;
	}
}
