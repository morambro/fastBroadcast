package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.AppController.SynchronizedDevicesList;
import it.unipd.fast.broadcast.location.LocationService;
import it.unipd.fast.broadcast.location.MockLocationService;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FastBroadcastActivity extends FragmentActivity implements GuiHandlerInterface {
	protected final String TAG = "it.unipd.fast.broadcast";

	private ServiceConnection locationServiceConn = new LocServiceConnection();
	private boolean isServiceBinded = false;
	//Handler to UI update from non-UI thread
	private Handler activityHandler;

	// Wi-fi Direct fields
	private Button sendToAllButton;
	private Button connectToAllButton;
	private IControllerComponent connectionController;
	private ListView devicesListView;


	//GuiHandler Implementation
	@Override
	public Handler getGuiHandler() {
		return activityHandler;
	}


	//ServiceConnection for LocationServiceListener
	class LocServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder binder) {
			isServiceBinded = true;
			//					locationService = ((LocationService.LocServiceBinder) binder).getService();
			((LocationService.LocServiceBinder) binder).getService().addLocationListener(locServiceListener);
			Log.d(TAG, this.getClass().getSimpleName()+": Location Service Bound");
		}

		public void onServiceDisconnected(ComponentName name) {
			//Service runs on the same process, should never be called.
			isServiceBinded = false;
		}

	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activityHandler = new Handler() {
			@SuppressWarnings("unchecked")
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case SHOW_TOAST_MSG:
					Toast.makeText(FastBroadcastActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
					break;

				case UPDATE_PEERS:
					savePeers((SynchronizedDevicesList)msg.obj);

				default:
					break;
				}
			}
		};
		Intent locService = new Intent(this, MockLocationService.class);
		locationServiceConn = new LocServiceConnection();
		boolean temp = this.bindService(locService, locationServiceConn, Context.BIND_AUTO_CREATE);
		Log.d(TAG, this.getClass().getSimpleName()+": Location Service binding status : "+temp);
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
	public void savePeers(SynchronizedDevicesList peers) {
		String[] new_list = new String[peers.size()];
		for(int i = 0; i < peers.size() ; i++){
			WifiP2pDevice dev = peers.get(i);
			new_list[i] = dev.deviceName;
		}
		devicesListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, new_list));

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
		if(isServiceBinded) {
			this.unbindService(locationServiceConn);
			isServiceBinded = false;
			Log.d(TAG, this.getClass().getSimpleName()+": location service unbound");
		}
		Log.d(TAG, this.getClass().getSimpleName()+": onDestroy called");
		connectionController.disconnect();
	}

	/**
	 * Does all the initial setup
	 * 
	 */
	private void setupGui() {
		connectToAllButton = (Button)this.findViewById(R.id.connect_to_all_button);
		sendToAllButton = (Button)this.findViewById(R.id.send_button);
		devicesListView = (ListView)this.findViewById(R.id.devices_list_view);

		sendToAllButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				connectionController.sendAlert();
			}
		});

		connectToAllButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connectionController.connectToAll();
			}
		});
	}

	private void startServices() {

	}
}
