package it.unipd.fast.broadcast.event.connectioninfo;

import android.net.wifi.p2p.WifiP2pInfo;
import it.unipd.fast.broadcast.event.IEvent;

public class WiFiInfoCollectedEvent implements IEvent {
	
	public WifiP2pInfo wifiConnectionInfo;
	
	public WiFiInfoCollectedEvent(WifiP2pInfo wifiConnectionInfo){
		this.wifiConnectionInfo = wifiConnectionInfo;
	}
}
