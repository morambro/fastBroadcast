package it.unipd.vanets.framework.wificonnection.receiver;

public abstract class AbstractPacketReceiver implements Runnable{
	protected final String TAG = "it.unipd.vanets.framework";
	
	protected final int UDP_PORT = 8889;
	protected final int TCP_PORT = 8888;
	
	protected boolean terminated = false;
	
	public void start(){
		new Thread(this).start();
	}
	
	abstract public void terminate();
}
