package it.unipd.fast.broadcast.wifi_connection;

import java.util.Map;


public interface IWiFiConnectionController {
	boolean isGroupOwner();
	String getGroupOwnerAddress();
	void setPeersIdIPmap(Map<String,String> peersMap);
}
