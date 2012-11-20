//package it.unipd.fast.broadcast;
//
//import it.unipd.fast.broadcast.location.LocationService;
//import it.unipd.fast.broadcast.location.LocationServiceListener;
//import it.unipd.fast.broadcast.wifi_connection.NewWiFiConnectionController;
//
//import java.util.List;
//
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.location.Location;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.support.v4.app.FragmentActivity;
//import android.util.Log;
//import android.view.Menu;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//public class MainActivity_Backup extends FragmentActivity implements LocationServiceListener, GuiHandlerInterface {
//	protected final String TAG = "it.unipd.fast.broadcast";
//	
//	//Handler to UI update from non-UI thread
//	private Handler activityHandler;
//	private Location curLocation = null;
//	private ServiceConnection serviceConn = null;
//	private boolean isServiceBinded = false;
//	
//	// Wi-fi Direct fields
//    private Button send_to_all_button;
//    private Button connect_to_all_button;
//    private NewWiFiConnectionController connection_controller;
//    private TextView found_devices;
//	
//	class LocServiceConnection implements ServiceConnection {
//
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			isServiceBinded = true;
//			Log.d(TAG, this.getClass().getSimpleName()+": Service Bound");
//			((LocationService.LocServiceBinder) service).getService().addLocationListener(MainActivity_Backup.this);
//		}
//
//		public void onServiceDisconnected(ComponentName name) {
//			//Service runs on the same process, should never be called.
//			isServiceBinded = false;
//		}
//		
//	}
//	
//	//GuiHandler Implementation
//	@Override
//	public Handler getGuiHandler() {
//		return activityHandler;
//	}
//	
//	//Service listener implementation
//	public void onLocationChanged(Location location) {
//		curLocation = location;
//	}
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        doBindService();
//        setContentView(R.layout.activity_main);
//        activityHandler = new Handler() {
//        	@SuppressWarnings("unchecked")
//			public void handleMessage(android.os.Message msg) {
//        		switch (msg.what) {
//				case SHOW_TOAST_MSG:
//					Toast.makeText(MainActivity_Backup.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
//					break;
//					
//				case UPDATE_PEERS:
//					savePeers((List<WifiP2pDevice>)msg.obj);
//
//				default:
//					break;
//				}
//        	}
//        };
//        connect_to_all_button = (Button)this.findViewById(R.id.connect_to_all_button);
//        send_to_all_button = (Button)this.findViewById(R.id.send_button);
//        found_devices = (TextView)this.findViewById(R.id.peers_list);
//        
//        
//        // Create fragment who shows found peers list, initially empty
////        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
////        ListFragment listFragment = new FragmentListProva(new ArrayList<WifiP2pDevice>(), do_on_peer_selected);
////        ft.add(R.id.fragment_container, listFragment, "List_Fragment");
////        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
////        ft.commit();
//        connection_controller = new NewWiFiConnectionController(this, this);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }
//    
//    
//    /**
//     * Interface used to exchange informations between fragment and this activity
//     * 
//     */
//    public static interface OnPeerSelectedCallback{
//    	public void doConnection(WifiP2pDevice device);
//    }
//    
//    
//    /**
//     * OnPeerSelectedCallback callback implementation
//     * 
//     */
//    private OnPeerSelectedCallback do_on_peer_selected =  new OnPeerSelectedCallback() {
//		
//		public void doConnection(WifiP2pDevice device) {
//			connection_controller.connect(device);
//		}
//	};
//	
//	 /**
//     * Metodo che riceve la lista aggiornata dei peers connessi e aggiorna il fragment che la mostra, impostando 
//     * l'handler per il click di un item della lista.
//     * 
//     * @param peers
//     */
//    public void savePeers(List<WifiP2pDevice> peers) {
////    	Toast.makeText(getApplicationContext(),"Updating the list", Toast.LENGTH_LONG).show();
////    	FragmentTransaction fragment_transaction = getSupportFragmentManager().beginTransaction();
////        ListFragment listFragment = new FragmentListProva(peers,do_on_peer_selected);
////        fragment_transaction.replace(R.id.fragment_container, listFragment, "List_Fragment");
////        fragment_transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
////        fragment_transaction.commit();
//    	String new_list = "";
//    	for(WifiP2pDevice dev : peers){
//    		new_list += ""+dev.deviceName+"\n" +
//    					""+dev.deviceAddress+"\n" +
//    					"Status : "+dev.status+"\n" +
//    					"-----------------------\n";
//    	}
//    	found_devices.setText(new_list);
//        
//    }
//    
//    
//    
//    @Override
//    protected void onDestroy() {
//    	super.onDestroy();
//    	doUnbindService();
//    	Log.d(TAG, this.getClass().getSimpleName()+": onDestroy called");
//		connection_controller.disconnect();
//    }
//    
//    private void doBindService() {
//    	if(isServiceBinded)
//    		return;
//    	Intent locService = new Intent(this, LocationService.class);
//    	serviceConn = new LocServiceConnection();
//    	boolean temp = bindService(locService, serviceConn, BIND_AUTO_CREATE);
//    	Log.d(TAG, this.getClass().getSimpleName()+": binding status: "+temp);
//    }
//    
//    private void doUnbindService() {
//    	if(!isServiceBinded)
//    		return;
//    	unbindService(serviceConn);
//	Log.d(TAG, this.getClass().getSimpleName()+": service unbound");
//    	isServiceBinded = false;
//    }
//    
//    @Override
//    protected void onResume() {
//    	super.onResume();
//    	connection_controller.setFastBroadCastReceiverRegistered(true);
//    }
//    
//    @Override
//    protected void onPause() {
//    	super.onPause();
//    	connection_controller.setFastBroadCastReceiverRegistered(false);
//    }
//    
//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//    	super.onPostCreate(savedInstanceState);
//    	connection_controller.discoverPeers();
////    	Log.d(TAG, this.getClass().getSimpleName()+": Setting button listener");
//    	send_to_all_button.setOnClickListener(new OnClickListener() {
//			
//			public void onClick(View v) {
////				Log.d(TAG, this.getClass().getSimpleName()+": button clicked");
//				connection_controller.sendBroadcast("<message type='2'><content>ALERT!!!</content></message>");
//			}
//		});
//    	
//    	connect_to_all_button.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				connection_controller.connectToAll();
//			}
//		});
//    }
//}
