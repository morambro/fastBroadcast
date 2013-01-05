package it.unipd.vanets.framework.wificonnection.connectioninfomanager;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;

public interface IConnectionInfoManager extends ConnectionInfoListener{
	/**
	 * Callback interface used to perform specific operation when info are available
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public interface OnConnectionInfoCollected{
		public void onInfoCollected(WifiP2pInfo info);
	}
}
