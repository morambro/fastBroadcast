package it.unipd.fastbroadcast;

import it.unipd.testbase.R;
import it.unipd.vanets.framework.AbstractMainActivity;
import it.unipd.vanets.framework.AppController.SynchronizedDevicesList;
import it.unipd.vanets.framework.eventdispatcher.EventDispatcher;
import it.unipd.vanets.framework.eventdispatcher.IComponent;
import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.eventdispatcher.event.SetupCompletedEvent;
import it.unipd.vanets.framework.eventdispatcher.event.protocol.SendAlertMessageEvent;
import it.unipd.vanets.framework.eventdispatcher.event.protocol.ShowSimulationResultsEvent;
import it.unipd.vanets.framework.eventdispatcher.event.protocol.StopSimulationEvent;
import it.unipd.vanets.framework.helper.LogPrinter;
import it.unipd.vanets.framework.wificonnection.message.IMessage;
import it.unipd.vanets.framework.wificonnection.transmissionmanager.PacketSenderFactory;
import it.unipd.vanets.framework.wificonnection.transmissionmanager.TransmissionManager.TransportSelectorFilter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TestBaseActivity extends AbstractMainActivity implements IComponent{
	protected final String TAG = "it.unipd.vanets.framework";
	
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
	
	/********************************* IMPLEMENTATION SPECIFIC OVERRIDED METHODS ***********************/

	@Override
	public void implementationOnCreate() {
		// First operation that must be done!
		super.setFilter(new TransportSelectorFilter() {
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
	protected void implementationOnDestroy() {
		super.onDestroy();
		EventDispatcher.getInstance().triggerEvent(new StopSimulationEvent(false,true));
		android.os.Process.killProcess(android.os.Process.myPid()); 
	}

	@Override
	protected void implementationOnPause() {
		// Do nothing implementation specific on pause
	}
	
	
	@Override
	protected void implementationOnResume() {
		// Do nothing implementation specific on resume
	}
	
	/*******************************************************************************************/
	
	/**
	 * Receives the devices list and updates the corrisponding textView
	 * 
	 * @param peers
	 */
	private void savePeers(SynchronizedDevicesList peers) {
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
