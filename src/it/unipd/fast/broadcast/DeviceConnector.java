package it.unipd.fast.broadcast;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import it.unipd.fast.broadcast.AppController.SynchronizedDevicesList;
import it.unipd.fast.broadcast.event.IEvent;

public class DeviceConnector implements Runnable,IComponent{
	protected final String TAG = "it.unipd.fast.broadcast";
	
	protected static final int MAX_ATTEMPTS = 	5;
	protected static final int MAX_WAIT 	=	500; 
	
	int currentDevice = 0;
	SynchronizedDevicesList peers;
	WifiP2pManager manager;
	AppController __controller;
	Channel channel;
	boolean requestConnectionSent;
	
	class DeviceConnectorThread extends Thread{
		
	}
	
	protected Object synchPoint = new Object();
	
	public DeviceConnector(SynchronizedDevicesList peers, WifiP2pManager manager,Channel channel,AppController __controller) {
		this.peers = peers;
		this.manager = manager;
		this.__controller = __controller;
		this.channel = channel;
		register();
	}
	
	public void run() {
		if(currentDevice >= peers.size())
			return;
		WifiP2pDevice device = peers.get(currentDevice);
		
		requestConnectionSent = false;
		
		int currentAttempts = 0;
		while(!requestConnectionSent && currentAttempts < MAX_ATTEMPTS){
			synchronized(synchPoint){
				requestConenction(device);
				try {
					synchPoint.wait(MAX_WAIT);
					if(requestConnectionSent) {
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
			currentAttempts++;
		}
		
		requestConenction(device);
		
		currentDevice++;
	}
	
	private void requestConenction(WifiP2pDevice device){
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		config.groupOwnerIntent = 15;
		manager.connect(channel,config, new ActionListener() {

			public void onSuccess() {
				requestConnectionSent = true;
				synchronized(synchPoint){
					synchPoint.notifyAll();
				}
			}

			public void onFailure(int reason) {
				requestConnectionSent = false;
				synchronized(synchPoint){
					synchPoint.notifyAll();
				}
			}
		});
	}
	
	@Override
	public void handle(IEvent event) {
		if(event instanceof ProceedWithNextEvent){
			// If there are more devices to connect to continue
			if(peers != null && currentDevice < peers.size()){
				Log.d(TAG,this.getClass().getSimpleName() + " : Proceed with peer "+currentDevice+" of "+peers.size());
				new Thread(this).start();
			}else{
				Log.d(TAG,this.getClass().getSimpleName()+" : No other peer to ask to");
			}
		}
	}
	
	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(ProceedWithNextEvent.class);
		EventDispatcher.getInstance().registerComponent(this,events);
	}
}