package it.unipd.fast.broadcast.wifi_connection.connectionmanager;

import java.util.Map;

import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;

public interface IConnectionInfoManager extends ConnectionInfoListener{
	/**
	 * Method used by WiFi controller to notify whether a new peer is added to the local controller map
	 * 
	 * @param map
	 * @param deviceNumber
	 */
	public void newPeerAddedNotification(Map<String,String> map, int deviceNumber);
}
