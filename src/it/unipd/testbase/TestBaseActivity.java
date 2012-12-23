package it.unipd.testbase;

import it.unipd.testbase.AppController.SynchronizedDevicesList;
import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.IComponent;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.SendAlertMessageEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.ShowSimulationResultsEvent;
import it.unipd.testbase.helper.DebugLogger;
import it.unipd.testbase.location.MockLocationService;
import it.unipd.testbase.protocol.FastBroadcastService;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;
import it.unipd.testbase.wificonnection.receiver.DataReceiverService;
import it.unipd.testbase.wificonnection.transmissionmanager.PacketSenderFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class TestBaseActivity extends FragmentActivity implements GuiHandlerInterface,IComponent {
	protected final String TAG = "it.unipd.testbase";
	
	private DebugLogger logger = new DebugLogger(TestBaseActivity.class);
	
	private final int TOTAL_SERVICES = 1;

	private ServiceConnection locationServiceConn = new LocServiceConnection();
	private int bindedServices = 0;

	private boolean isLocationServiceBinded = false;

	private Handler activityHandler;

	// Wi-fi Direct fields
	private Button sendToAllButton;
	private Button connectToAllButton;
	private IControllerComponent controller;
	private ListView devicesListView;


	//GuiHandler Implementation
	@Override
	public Handler getGuiHandler() {
		return activityHandler;
	}


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

//	/**
//	 * Connection class for Fast Broadcast Service
//	 * 
//	 * @author Moreno Ambrosin
//	 *
//	 */
//	private class FastBroadcastServiceConnection implements ServiceConnection{
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder binder) {
//			serviceCreated();
//			Log.d(TAG, FastBroadcastServiceConnection.class.getSimpleName()+": Fast Broadcast Service requestConnectionSent");
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			Log.d(TAG, FastBroadcastServiceConnection.class.getSimpleName()+": Fast Broadcast Service lost");
//		}
//	}
//
//	/**
//	 * Connection class for Data Receiver Service
//	 * 
//	 * @author Moreno Ambrosin
//	 *
//	 */
//	private class DataServiceConnection implements ServiceConnection {
//
//		@Override
//		public void onServiceConnected(ComponentName arg0, IBinder binder) {
//			serviceCreated();
//			Log.d(TAG, this.getClass().getSimpleName()+": DataReceiverService binded");
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName arg0) {
//			Log.d(TAG, this.getClass().getSimpleName()+": Service lost");
//		}
//
//	}


//	private void startFastBroadcastService() {
//		Log.d(TAG, this.getClass().getSimpleName()+": Bindo il servizio di Range Estimation");
//		Intent estimationService = new Intent(this, FastBroadcastService.class);
//		this.startService(estimationService);
//		this.bindService(estimationService, fastBroadcastServiceConnection, Context.BIND_AUTO_CREATE);
//	}

	private void startLocationService(){
		Intent locService = new Intent(this, MockLocationService.class);
		locationServiceConn = new LocServiceConnection();
		boolean temp = this.bindService(locService, locationServiceConn, Context.BIND_AUTO_CREATE);
		logger.d("Location Service binding status : "+temp);
	}

//	private void startDataReceiverService(){
//		//Start DataReceiverService
//		Log.d(TAG, this.getClass().getSimpleName()+": Bindo il servizio di ricezione dati");
//		Intent dataService = new Intent(this, DataReceiverService.class);
//		this.startService(dataService);
//		boolean f = this.bindService(dataService, dataReceiverServiceConnection, Context.BIND_AUTO_CREATE);
//	}

	/**
	 * Handler class to implement Interprocess communication
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	static class ActivityHandler extends Handler{
		
		private WeakReference<TestBaseActivity> activity;
		
		public ActivityHandler(TestBaseActivity activity) {
			this.activity = new WeakReference<TestBaseActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SHOW_TOAST_MSG:
					Toast.makeText(activity.get(), msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;
	
				case UPDATE_PEERS:
					activity.get().savePeers((SynchronizedDevicesList)msg.obj);
	
				default:
					break;
			}
		}
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		register();
		setContentView(R.layout.activity_main);
		activityHandler = new ActivityHandler(this);
		setupGui();
		FastBroadcastService.getInstance();
		DataReceiverService.getInstance();
		startLocationService();
//		startFastBroadcastService();
//		startDataReceiverService();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/**
	 * Receives the devices list and updates the textView
	 * 
	 * @param peers
	 */
	public void savePeers(SynchronizedDevicesList peers) {
		String[] new_list = new String[peers.size()];
		for(int i = 0; i < peers.size() ; i++){
			WifiP2pDevice dev = peers.get(i);
			String status = "";
			switch(dev.status){
				case WifiP2pDevice.AVAILABLE : status = "Available";break;
				case WifiP2pDevice.CONNECTED : status = "Connected";break;
				case WifiP2pDevice.FAILED : status = "Failed";break;
				case WifiP2pDevice.INVITED : status = "Invited";break;
				case WifiP2pDevice.UNAVAILABLE : status = "Unavailable";break;
			}
			new_list[i] = dev.deviceName + "  (" + status +")";
		}
		devicesListView.setAdapter(
				new ArrayAdapter<String>(
						this, 
						android.R.layout.simple_list_item_1, 
						android.R.id.text1,
						new_list
				)
		);
		
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

	@Override
	protected void onDestroy() {
		if(isLocationServiceBinded) {
			unbindService(locationServiceConn);
			isLocationServiceBinded = false;
			logger.d("location service unbound");
		}
		FastBroadcastService.getInstance().stopExecuting();
		DataReceiverService.getInstance().stopExecuting();
//		unbindService(fastBroadcastServiceConnection);
//		unbindService(dataReceiverServiceConnection);
		logger.d("onDestroy called");
		controller.disconnect();
		super.onDestroy();
	}

	/**
	 * Does all the initial setup
	 * 
	 */
	private void setupGui() {
		connectToAllButton 	= (Button)this.findViewById(R.id.connect_to_all_button);
		sendToAllButton 	= (Button)this.findViewById(R.id.send_button);
		devicesListView 	= (ListView)this.findViewById(R.id.devices_list_view);
		
		Button sendBroadUdp = (Button)this.findViewById(R.id.debugSendUDP);
		
		sendBroadUdp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				IMessage message = MessageBuilder.getInstance().getMessage(5,"192.168.49.255","ciaoooo");
				message.addContent("ciao", "ciao");
				message.prepare();
				PacketSenderFactory.getInstance().getTransmissionManager(PacketSenderFactory.UNRELIABLE_TRANSPORT)
				.send("192.168.49.255", message);
			}
		});
		
		sendToAllButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
//				controller.sendAlert();
				EventDispatcher.getInstance().triggerEvent(new SendAlertMessageEvent(0));
			}
		});

		connectToAllButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				controller.connectToAll();
			}
		});
	}

	private synchronized void serviceCreated() {
		bindedServices++;
		if(bindedServices == TOTAL_SERVICES) {
			controller = new AppController(this, this);
			controller.setFastBroadCastReceiverRegistered(true);
		}
	}

	@Override
	public void handle(IEvent event) {
		if(event.getClass().equals(ShowSimulationResultsEvent.class)){
			if(!SimulationResultsActivity.isOpened) {
				Intent myIntent = new Intent(this, SimulationResultsActivity.class);
				this.startActivity(myIntent);
			}
		}
	}

	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(ShowSimulationResultsEvent.class);
		EventDispatcher.getInstance().registerComponent(this,events);
	}
}
