package it.unipd.fast.broadcast.wifi_connection.connectionmanager;

import it.unipd.fast.broadcast.wifi_connection.connectionmanager.ConnectionInfoManager.OnConnectionInfoCollected;

public class ConnectionManagerFactory {
private static final ConnectionManagerFactory singleton = new ConnectionManagerFactory();
	
	private ConnectionManagerFactory() {}
	
	public static ConnectionManagerFactory getInstance() {
		return singleton;
	}
	
	public ConnectionInfoManager getConnectionManager(OnConnectionInfoCollected callback) {
		return new ConnectionInfoManager(callback);
	}
}
