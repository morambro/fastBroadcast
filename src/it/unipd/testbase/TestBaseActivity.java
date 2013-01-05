package it.unipd.testbase;

import it.unipd.testbase.AppController.SynchronizedDevicesList;
import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.IComponent;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.SetupCompletedEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.SendAlertMessageEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.ShowSimulationResultsEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.StopSimulationEvent;
import it.unipd.testbase.helper.LogPrinter;
import it.unipd.testbase.protocol.FastBroadcastService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TestBaseActivity extends AbstractMainActivity implements IComponent{
	protected final String TAG = "it.unipd.testbase";
	
	private Handler activityHandler;

	// Wi-fi Direct fields
	private Button sendToAllButton;
	private Button connectToAllButton;

	private ListView devicesListView;
	private Button resetSimulation;
	private TextView statusTextView;
	private ScrollView progressScrollView;

	
	@Override
	public Handler getGuiHandler() {
		return activityHandler;
	}


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
				case SHOW_TOAST_MESSAGE:
					Toast.makeText(activity.get(), msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;
	
				case UPDATE_PEERS:
					activity.get().savePeers((SynchronizedDevicesList)msg.obj);
					break;
	
				case PROGRESS_MESSAGE:
					activity.get().writeOnStatus((String)msg.obj);
					break;
					
				default:
					break;
			}
		}
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activityHandler = new ActivityHandler(this);
		setupGui();
		/**
		 * Force Fast Broadcast Service creation
		 */
		FastBroadcastService.getInstance();
		register();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/**
	 * Receives the devices list and updates the corrisponding textView
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
	protected void onDestroy() {
		super.onDestroy();
		EventDispatcher.getInstance().triggerEvent(new StopSimulationEvent(false,true));
		android.os.Process.killProcess(android.os.Process.myPid()); 
	}

	/**
	 * Does all the initial setup
	 * 
	 */
	private void setupGui() {
		connectToAllButton 	= (Button)this.findViewById(R.id.connect_to_all_button);
		sendToAllButton 	= (Button)this.findViewById(R.id.send_button);
		sendToAllButton.setEnabled(false);
		devicesListView 	= (ListView)this.findViewById(R.id.devices_list_view);
		resetSimulation 	= (Button)this.findViewById(R.id.debugSendUDP);
		statusTextView 		= (TextView)this.findViewById(R.id.ProgressInfo);
		progressScrollView	= (ScrollView)this.findViewById(R.id.ProgressScroll);
		
		resetSimulation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendToAllButton.setEnabled(true);
				LogPrinter.getInstance().reset();
			}
		});
		
		sendToAllButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Send an Alert message in broadcast
				EventDispatcher.getInstance().triggerEvent(new SendAlertMessageEvent(0));
				resetSimulation.setEnabled(false);
			}
		});

		connectToAllButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				controller.connectToAll();
			}
		});
	}

	/**
	 * Writes the given message to status view 
	 * 
	 * @param message
	 */
	private void writeOnStatus(String message){
		statusTextView.append(message+"\n");
		scrollToBottom();
	}
	
	/**
	 * Scrolls progress view
	 */
	private void scrollToBottom() {
	    progressScrollView.post(new Runnable() { 
	        public void run() { 
	            progressScrollView.smoothScrollTo(0, statusTextView.getBottom());
	        } 
	    });
	}
	
	@Override
	public void handle(IEvent event) {
		if(event.getClass().equals(ShowSimulationResultsEvent.class)){
			runOnUiThread(new Runnable(){
				public void run() {
					resetSimulation.setEnabled(true);
					sendToAllButton.setEnabled(false);
				}
			});
			if(!SimulationResultsActivity.isOpened) {
				Intent myIntent = new Intent(this, SimulationResultsActivity.class);
				this.startActivity(myIntent);
			}
			return;
		}
		if(event.getClass().equals(SetupCompletedEvent.class)){
			runOnUiThread(new Runnable(){
				public void run() {
					connectToAllButton.setEnabled(false);
				};
			});
			return;
		}
	}

	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(ShowSimulationResultsEvent.class);
		events.add(SetupCompletedEvent.class);
		EventDispatcher.getInstance().registerComponent(this,events);
	}
}
