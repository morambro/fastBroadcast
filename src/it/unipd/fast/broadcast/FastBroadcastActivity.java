package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;

import java.util.List;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FastBroadcastActivity extends FragmentActivity implements GuiHandlerInterface {
	protected final String TAG = "it.unipd.fast.broadcast";

	//Handler to UI update from non-UI thread
	private Handler activityHandler;
//	private Location curLocation = null;
//	private ServiceConnection serviceConn = null;
//	private boolean isServiceBinded = false;

	// Wi-fi Direct fields
	private Button sendToAllButton;
	private Button connectToAllButton;
	private IAppController connectionController;
	private TextView foundDevices;

//	private LocServiceBroadcastInterface locationService;
//	
//	//ServiceConnection for LocationServiceListener
//	class LocServiceConnection implements ServiceConnection {
//
//		public void onServiceConnected(ComponentName name, IBinder binder) {
//			isServiceBinded = true;
//			locationService = ((LocationService.LocServiceBinder) binder).getService();
//			((LocationService.LocServiceBinder) binder).getService().addLocationListener(FastBroadcastActivity.this);
//			Log.d(TAG, this.getClass().getSimpleName()+": Location Service Bound");
//		}
//
//		public void onServiceDisconnected(ComponentName name) {
//			//Service runs on the same process, should never be called.
//			isServiceBinded = false;
//		}
//
//	}

	//GuiHandler Implementation
	@Override
	public Handler getGuiHandler() {
		return activityHandler;
	}

//	//Service listener implementation
//	public void onLocationChanged(Location location) {
//		curLocation = location;
//		Log.d(TAG,FastBroadcastActivity.class.getSimpleName() + " : " + location);
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
//		doBindService();
		setContentView(R.layout.activity_main);
		activityHandler = new Handler() {
			@SuppressWarnings("unchecked")
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case SHOW_TOAST_MSG:
					Toast.makeText(FastBroadcastActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;

				case UPDATE_PEERS:
					savePeers((List<WifiP2pDevice>)msg.obj);

				default:
					break;
				}
			}
		};
		connectionController = new AppController(this, this);
		setupGui();
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
	public void savePeers(List<WifiP2pDevice> peers) {
		String new_list = "";
		for(WifiP2pDevice dev : peers){
			new_list += ""+dev.deviceName+"\n" +
					""+dev.deviceAddress+"\n" +
					"Status : "+dev.status+"\n" +
					"-----------------------\n";
		}
		foundDevices.setText(new_list);

	}

	@Override
	protected void onResume() {
		super.onResume();
		connectionController.setFastBroadCastReceiverRegistered(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		connectionController.setFastBroadCastReceiverRegistered(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		doUnbindService();
		Log.d(TAG, this.getClass().getSimpleName()+": onDestroy called");
		connectionController.disconnect();
	}

//	private void doBindService() {
//		if(isServiceBinded)
//			return;
//		Intent locService = new Intent(this, MockLocationService.class);
//		serviceConn = new LocServiceConnection();
//		boolean temp = bindService(locService, serviceConn, BIND_AUTO_CREATE);
//		Log.d(TAG, this.getClass().getSimpleName()+": binding status: "+temp);
//	}
//
//	private void doUnbindService() {
//		if(!isServiceBinded)
//			return;
//		unbindService(serviceConn);
//		Log.d(TAG, this.getClass().getSimpleName()+": service unbound");
//		isServiceBinded = false;
//	}
	
	/**
	 * Does all the initial setup
	 * 
	 */
	private void setupGui() {
		connectToAllButton = (Button)this.findViewById(R.id.connect_to_all_button);
		sendToAllButton = (Button)this.findViewById(R.id.send_button);
		foundDevices = (TextView)this.findViewById(R.id.peers_list);
		
		sendToAllButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				connectionController.sendBroadcast(
					MessageBuilder.getInstance().getMessage("" +
							"<message type='2' recipient_id='255.255.255.255'>" +
								"<content> ALERT!!! </content>" +
							"</message>")
				);
			}
		});

		connectToAllButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connectionController.connectToAll();
			}
		});
	}
}
