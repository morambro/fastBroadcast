package it.unipd.testbase.wificonnection.connectionmanager;


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
