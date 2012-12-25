package it.unipd.testbase.wificonnection.receiver;

public abstract class AbstractPacketReceiver implements Runnable{
	protected final String TAG = "it.unipd.testbase";
	
	protected boolean terminated = false;
	
	public void start(){
		new Thread(this).start();
	}
	
	abstract public void terminate();
}
