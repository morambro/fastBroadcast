package it.unipd.testbase;

import it.unipd.testbase.helper.DebugLogger;
import it.unipd.testbase.location.MockLocationService;
import it.unipd.testbase.protocol.FastBroadcastService;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.receiver.DataReceiverService;
import it.unipd.testbase.wificonnection.transmissionmanager.PacketSenderFactory;
import it.unipd.testbase.wificonnection.transmissionmanager.TransmissionManager.TransportSelectorFilter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

/**
 * Abstract class to be subclassed to realize an Activity for a testbed application.
 * The Activity which realizes this class can override every FragmentActivity method, and to
 * implement getGuiHandler method to provide inter-process communication to update the view.
 * 
 * @author Moreno Ambrosin
 *
 */
public abstract class AbstractMainActivity extends FragmentActivity implements GuiHandlerInterface {
	
	protected DebugLogger logger = new DebugLogger(AbstractMainActivity.class);
	
	/**
	 * The number of total Android services 
	 */
	protected final int TOTAL_SERVICES = 1;

	/**
	 * Services involved in Framework
	 *  
	 */
	protected ServiceConnection locationServiceConn = new LocServiceConnection();
	protected DataReceiverService dataReceiver;
	
	/**
	 * Application Controller 
	 */
	protected IControllerComponent controller;
	
	/**
	 * Number of currently binded services
	 * 
	 */
	protected int bindedServices = 0;

	/**
	 * Tells weather location service is binded
	 */
	private boolean isLocationServiceBinded = false;
	
	/**
	 * Connection class for Location Service
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	class LocServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder binder) {
			isLocationServiceBinded = true;
			serviceCreated();
			logger.d("Location Service Bound");
		}

		public void onServiceDisconnected(ComponentName name) {
			isLocationServiceBinded = false;
		}

	}
	
	/**
	 * Starts the Location Service
	 * 
	 */
	private void startLocationService(){
		Intent locService = new Intent(this, MockLocationService.class);
		locationServiceConn = new LocServiceConnection();
		boolean temp = this.bindService(locService, locationServiceConn, Context.BIND_AUTO_CREATE);
		logger.d("Location Service binding statusTextView : "+temp);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Data Receiver Service creation
		dataReceiver = new DataReceiverService();
		
		// Starting Location Service
		startLocationService();
	}	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		logger.d("Main Activity Desctruction");
		if(isLocationServiceBinded) {
			unbindService(locationServiceConn);
			isLocationServiceBinded = false;
			logger.d("location service unbound");
		}
		// Stopping data receiver
		dataReceiver.stopExecuting();
		// Calling disconnect 
		controller.disconnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(controller != null){
			controller.setFastBroadCastReceiverRegistered(true);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(controller != null){
			controller.setFastBroadCastReceiverRegistered(false);
		}
	}
	
	/**
	 * On services creation
	 * 
	 */
	protected synchronized void serviceCreated() {
		bindedServices++;
		if(bindedServices == TOTAL_SERVICES) {
			controller = new AppController(this, this, new TransportSelectorFilter() {
				@Override
				public int getTransportForMessage(IMessage message) {
					if(
						message.getType() == FastBroadcastService.HELLO_MESSAGE_TYPE
					
							){
						return PacketSenderFactory.UNRELIABLE_TRANSPORT;
					}
					return PacketSenderFactory.RELIABLE_TRANSPORT;
				}
			});
			controller.setFastBroadCastReceiverRegistered(true);
		}
	}
}
