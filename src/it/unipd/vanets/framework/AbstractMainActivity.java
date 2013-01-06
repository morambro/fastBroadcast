package it.unipd.vanets.framework;

import it.unipd.testbase.R;
import it.unipd.vanets.framework.helper.DebugLogger;
import it.unipd.vanets.framework.location.MockLocationService;
import it.unipd.vanets.framework.wificonnection.receiver.DataReceiverService;
import it.unipd.vanets.framework.wificonnection.transmissionmanager.TransmissionManager.TransportSelectorFilter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

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
	
	private TransportSelectorFilter filter;
	
	protected void setFilter(TransportSelectorFilter filter){
		this.filter = filter;
	}
	
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
	
	
	/**************************** ACTIVITY METHODS *************************************/
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Data Receiver Service creation
		dataReceiver = new DataReceiverService();
		
		// Starting Location Service
		startLocationService();
		implementationOnCreate();
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
		implementationOnDestroy();
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		if(controller != null){
			controller.setFastBroadCastReceiverRegistered(true);
		}
		implementationOnResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(controller != null){
			controller.setFastBroadCastReceiverRegistered(false);
		}
		implementationOnPause();
	}
	
	/**
	 * Methods to define to perform graphical initializations and other
	 * implementation specific operations
	 */
	abstract protected void implementationOnCreate();
	abstract protected void implementationOnDestroy();
	abstract protected void implementationOnResume();
	abstract protected void implementationOnPause();
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/***********************************************************************++
	
	/**
	 * On services creation
	 * 
	 */
	protected synchronized void serviceCreated() {
		bindedServices++;
		if(bindedServices == TOTAL_SERVICES) {
			// Create Controller, setting the filter.
			controller = new AppController(this, this,filter);
			controller.setFastBroadCastReceiverRegistered(true);
		}
	}
}
