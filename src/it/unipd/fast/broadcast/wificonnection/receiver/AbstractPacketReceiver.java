package it.unipd.fast.broadcast.wificonnection.receiver;

public abstract class AbstractPacketReceiver implements Runnable{
	protected final String TAG = "it.unipd.fast.broadcast";
	
	protected boolean terminated = false;
	
	public void start(){
		new Thread(this).start();
	}
	
	abstract public void terminate();
}
