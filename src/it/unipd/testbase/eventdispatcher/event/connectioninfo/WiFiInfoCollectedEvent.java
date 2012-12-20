package it.unipd.testbase.eventdispatcher.event.connectioninfo;

import android.net.wifi.p2p.WifiP2pInfo;
import it.unipd.testbase.eventdispatcher.event.IEvent;

public class WiFiInfoCollectedEvent implements IEvent {
	
	public WifiP2pInfo wifiConnectionInfo;
	
	public WiFiInfoCollectedEvent(WifiP2pInfo wifiConnectionInfo){
		this.wifiConnectionInfo = wifiConnectionInfo;
	}
}
