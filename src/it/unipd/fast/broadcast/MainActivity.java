package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.location.LocServiceBroadcastInterface;
import it.unipd.fast.broadcast.location.LocationService;
import it.unipd.fast.broadcast.location.LocationServiceListener;
import it.unipd.fast.broadcast.location.MockLocationService;
import it.unipd.fast.broadcast.wifi_connection.WiFiConnectionController;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements LocationServiceListener, GuiHandlerInterface {
	protected final String TAG = "it.unipd.fast.broadcast";

	//Handler to UI update from non-UI thread
	private Handler activityHandler;
	private Location curLocation = null;
	private ServiceConnection serviceConn = null;
	private boolean isServiceBinded = false;

	// Wi-fi Direct fields
	private Button send_to_all_button;
	private Button connect_to_all_button;
	private WiFiConnectionController connection_controller;
	private TextView found_devices;

	private LocServiceBroadcastInterface locationService;
	
	//ServiceConnection for LocationServiceListener
	class LocServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder binder) {
			isServiceBinded = true;
			locationService = ((LocationService.LocServiceBinder) binder).getService();
			((LocationService.LocServiceBinder) binder).getService().addLocationListener(MainActivity.this);
			Log.d(TAG, this.getClass().getSimpleName()+": Location Service Bound");
		}

		public void onServiceDisconnected(ComponentName name) {
			//Service runs on the same process, should never be called.
			isServiceBinded = false;
		}

	}

	//GuiHandler Implementation
	@Override
	public Handler getGuiHandler() {
		return activityHandler;
	}

	//Service listener implementation
	public void onLocationChanged(Location location) {
		curLocation = location;
		Log.d(TAG,MainActivity.class.getSimpleName() + " : " + location);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		doBindService();
		setContentView(R.layout.activity_main);
		activityHandler = new Handler() {
			@SuppressWarnings("unchecked")
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case SHOW_TOAST_MSG:
					Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;

				case UPDATE_PEERS:
					savePeers((List<WifiP2pDevice>)msg.obj);

				default:
					break;
				}
			}
		};
		connection_controller = new WiFiConnectionController(this, this);
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
		found_devices.setText(new_list);

	}

	@Override
	protected void onResume() {
		super.onResume();
		connection_controller.setFastBroadCastReceiverRegistered(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		connection_controller.setFastBroadCastReceiverRegistered(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
		Log.d(TAG, this.getClass().getSimpleName()+": onDestroy called");
		connection_controller.disconnect();
	}

	private void doBindService() {
		if(isServiceBinded)
			return;
		Intent locService = new Intent(this, MockLocationService.class);
		serviceConn = new LocServiceConnection();
		boolean temp = bindService(locService, serviceConn, BIND_AUTO_CREATE);
		Log.d(TAG, this.getClass().getSimpleName()+": binding status: "+temp);
	}

	private void doUnbindService() {
		if(!isServiceBinded)
			return;
		unbindService(serviceConn);
		Log.d(TAG, this.getClass().getSimpleName()+": service unbound");
		isServiceBinded = false;
	}
	
	/**
	 * Does all the initial setup
	 * 
	 */
	private void setupGui() {
		connect_to_all_button = (Button)this.findViewById(R.id.connect_to_all_button);
		send_to_all_button = (Button)this.findViewById(R.id.send_button);
		found_devices = (TextView)this.findViewById(R.id.peers_list);
		
		send_to_all_button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				connection_controller.sendBroadcast(
					MessageBuilder.getInstance().getMessage("" +
							"<message type='2' recipient_id='255.255.255.255'>" +
								"<content> ALERT!!! </content>" +
							"</message>")
				);
			}
		});

		connect_to_all_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connection_controller.connectToAll();
			}
		});
	}
}
