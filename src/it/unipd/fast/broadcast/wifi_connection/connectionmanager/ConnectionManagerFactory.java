package it.unipd.fast.broadcast.wifi_connection.connectionmanager;


public class ConnectionManagerFactory {
private static final ConnectionManagerFactory singleton = new ConnectionManagerFactory();
	
	private ConnectionManagerFactory() {}
	
	public static ConnectionManagerFactory getInstance() {
		return singleton;
	}
	
	public ConnectionInfoManager getConnectionManager() {
		return new ConnectionInfoManager();
	}
}
