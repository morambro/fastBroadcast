package it.unipd.fast.broadcast.wifi_connection;

import it.unipd.fast.broadcast.GuiHandlerInterface;
import it.unipd.fast.broadcast.location.LocationService;
import it.unipd.fast.broadcast.location.LocationServiceListener;
import it.unipd.fast.broadcast.location.MockLocationProvider;
import it.unipd.fast.broadcast.location.MockLocationService;
import it.unipd.fast.broadcast.protocol_implementation.FastBroadcastService;
import it.unipd.fast.broadcast.protocol_implementation.FastBroadcastService.FastBroadcastServiceBinder;
import it.unipd.fast.broadcast.protocol_implementation.ICommunicationHandler;
import it.unipd.fast.broadcast.protocol_implementation.ICommunicationHandler.OnForwardedHandler;
import it.unipd.fast.broadcast.wifi_connection.connectionmanager.ConnectionInfoManager.OnConnectionInfoCollected;
import it.unipd.fast.broadcast.wifi_connection.connectionmanager.ConnectionManagerFactory;
import it.unipd.fast.broadcast.wifi_connection.connectionmanager.IConnectionInfoManager;
import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService.DataReceiverBinder;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService.IDataCollectionHandler;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverServiceInterface;
import it.unipd.fast.broadcast.wifi_connection.receiver.FastBroadcastReceiver;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class WiFiConnectionController implements IWiFiConnectionController{

	/********************************************** DECLARATIONS *************************************************/

	protected final String TAG = "it.unipd.fast.broadcast";
	public static String MAC_ADDRESS = null;
	
	
	private MockLocationProvider __mock_provider;
	

	private Handler guiHandler;
	private ServiceConnection dataReceiverServiceConnection = new DataServiceConnection();
	private ServiceConnection fastBroadcastServiceConnection = new FastBroadcastServiceConnection();
	private ServiceConnection serviceConn = new LocServiceConnection();
	private boolean isServiceBinded = false;
	private Location currentLocation;
	private IDataCollectionHandler collectionHandler = new CollectionHandler();
	private DataReceiverServiceInterface dataInterface = null;
	private WifiP2pManager manager;
	private Channel channel;
	private BroadcastReceiver receiver;
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private Map<String,String> peerIdIpMap;
	private Context context;
	private IntentFilter intentFilter;
	private boolean mapSent = false;

	private String groupOwnerAddress;
	private boolean isGroupOwner = false;

	private ICommunicationHandler fastBroadcastService;

	/************************************************* INTERFACES/CLASSES ********************************************/
	
	//ServiceConnection for LocationServiceListener
	class LocServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder binder) {
			isServiceBinded = true;
//			locationService = ((LocationService.LocServiceBinder) binder).getService();
			((LocationService.LocServiceBinder) binder).getService().addLocationListener(locServiceListener);
			__mock_provider = ((LocationService.LocServiceBinder) binder).getService().__get_mock_provider();
			Log.d(TAG, this.getClass().getSimpleName()+": Got MockProvider: "+__mock_provider.name);
			Log.d(TAG, this.getClass().getSimpleName()+": Location Service Bound");
		}

		public void onServiceDisconnected(ComponentName name) {
			//Service runs on the same process, should never be called.
			isServiceBinded = false;
		}

	}

	private class DataServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			dataInterface = ((DataReceiverBinder)binder).getService();
			dataInterface.registerHandler(collectionHandler);
			Log.d(TAG, this.getClass().getSimpleName()+": Servizio ricezione dati binded");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, this.getClass().getSimpleName()+": Service lost");
		}

	}

	private class FastBroadcastServiceConnection implements ServiceConnection{
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			fastBroadcastService = ((FastBroadcastServiceBinder)binder).getService();
			// After creating the service, update the location
			fastBroadcastService.setCurrentLocation(currentLocation);
			fastBroadcastService.setOnforwardedHadler(new OnForwardedHandler() {
				
				@Override
				public void doOnForwarded() {
					__mock_provider.updateLocation();
				}
			});
			Log.d(TAG, this.getClass().getSimpleName()+": Servizio ricezione dati binded " +
					"\nlocation = "+currentLocation.getLatitude()+","+currentLocation.getLongitude());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, this.getClass().getSimpleName()+": Estimation Service lost");
		}
	}

	/**
	 * PeerListListener implementation
	 * 
	 */
	private PeerListListener peerListener = new PeerListListener() {

		public void onPeersAvailable(WifiP2pDeviceList peers_list) {

			// Adds only new devices
			for(WifiP2pDevice device : peers_list.getDeviceList()){
				if(!peers.contains(device)){
					Log.d(TAG,this.getClass().getSimpleName() + "Aggiunto device");
					peers.add(device);
				}else{
					Log.d(TAG,this.getClass().getSimpleName() + "Device already in the list");
				}
			}
			// remove disappeared devices
			for(WifiP2pDevice device : peers){
				if(!peers_list.getDeviceList().contains(device)){
					Log.d(TAG,this.getClass().getSimpleName() + "Device not in the list, removed");
					peers.remove(device);
				}
			}

			Message msg = new Message();
			msg.obj = peers;
			msg.what = GuiHandlerInterface.UPDATE_PEERS;
			guiHandler.sendMessage(msg);

			Log.d(TAG, this.getClass().getSimpleName()+": Peers Added to the List");
		}

	};

	/**
	 * Called when connection info are available
	 * 
	 */
	private OnConnectionInfoCollected connectionInfoCallback = new OnConnectionInfoCollected() {

		@Override
		public void onInfoCollected(WifiP2pInfo info) {
			groupOwnerAddress = info.groupOwnerAddress.getCanonicalHostName();
			isGroupOwner = info.isGroupOwner;
		}
	};

	private LocationServiceListener locServiceListener = new LocationServiceListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG,WiFiConnectionController.class.getSimpleName() + " : " + location.getLatitude()+"; "+location.getLongitude());
			currentLocation = location;
			if(fastBroadcastService != null) fastBroadcastService.setCurrentLocation(location);
		}
	};

	// Listener used to be notified, when connection info are available
	private IConnectionInfoManager connectionInfoListener = ConnectionManagerFactory.getInstance().getConnectionManager(connectionInfoCallback);


	/******************************************************* METHODS ************************************************/

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public WiFiConnectionController(Context context, GuiHandlerInterface guiHandlerInterface) {
		this.context = context;
		this.guiHandler = guiHandlerInterface.getGuiHandler();
		collectionHandler.setWiFiController(this);
		// manager and channel initialization
		manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(context, context.getMainLooper(), null);
		// Register intent filter to receive specific intents
		intentFilter = new IntentFilter();
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		//Start DataReceiverService
		Log.d(TAG, this.getClass().getSimpleName()+": Bindo il servizio di ricezione dati");
		Intent dataService = new Intent(context, DataReceiverService.class);
		context.startService(dataService);
		context.bindService(dataService, dataReceiverServiceConnection, Context.BIND_AUTO_CREATE);
		
		//Start LocationService
		Intent locService = new Intent(context, MockLocationService.class);
		serviceConn = new LocServiceConnection();
		boolean temp = context.bindService(locService, serviceConn, Context.BIND_AUTO_CREATE);
		Log.d(TAG, this.getClass().getSimpleName()+": Location Service binding status : "+temp);

		// Setting static field which contains device MAC address
		MAC_ADDRESS = getDeviceMacAddress();
		Log.d(TAG, this.getClass().getSimpleName()+": il MAC address del dispositivo è = "+MAC_ADDRESS);
	}

	/**
	 * Method used to get device's mac address
	 * 
	 * @return
	 */
	protected String getDeviceMacAddress(){
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		android.net.wifi.WifiInfo wifiInf = wifiMan.getConnectionInfo();
		return wifiInf.getMacAddress();
	}


	/**
	 * Manages connection to the given device
	 * 
	 * @param device
	 */
	public void connect(WifiP2pDevice device){
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		config.groupOwnerIntent = 15;
		manager.connect(channel,config, new ActionListener() {

			public void onSuccess() {
				showToast("Richiesta di connessione effettuata");
			}

			public void onFailure(int reason) {
				showToast("Impossibil Connettersi reason = "+reason);
			}
		});


	}

	/**
	 * Tell whether register/unregister FastBroadcastReceiver
	 * 
	 * @param registered
	 */
	public void setFastBroadCastReceiverRegistered(boolean registered){
		if(registered) {
			receiver = new FastBroadcastReceiver(manager, channel,peerListener,connectionInfoListener);
			context.registerReceiver(receiver, intentFilter);
		}else{
			context.unregisterReceiver(receiver);
		}
	}

	/**
	 * Registers BroadcastReceiver and requests DataReceiverService
	 */
	public void aquireResources() {
		receiver = new FastBroadcastReceiver(manager, channel,peerListener,this.connectionInfoListener);
		context.registerReceiver(receiver, intentFilter);
	}

	/**
	 * Unregisters BroadcastReceiver and releases DataReceiverService
	 */
	public void releaseResources() {
		context.unregisterReceiver(receiver);
	}

	/**
	 * Setter method for the map. If current device is the GroupOwner, it sends back to
	 * all the devices connected to his group the complete map of all the peers
	 * 
	 * @param map
	 */
	public void setPeersIdIPmap(Map<String,String> map){

		if(peerIdIpMap == null){
			peerIdIpMap = map;
		}else{
			peerIdIpMap.putAll(map);
		}
		if(peerIdIpMap.containsKey(MAC_ADDRESS))
			peerIdIpMap.remove(MAC_ADDRESS);
		// If I'm the group owner and I haven't sent the map to all yet and the map is complete (all peers sent me their ID)
		// Broadcast the map to all, after adding my <ID,IP> to it!
		Map<String,String> mapToBroadcast = null;
		if(isGroupOwner){
			if(!mapSent && peerIdIpMap.keySet().size() == peers.size()){
				MockLocationProvider.__set_static_couter(0, peerIdIpMap.size());
				__mock_provider.updateLocation();
				Log.d(TAG,this.getClass().getCanonicalName()+": Invio la mappa a tutti : \n");
				mapToBroadcast = new HashMap<String, String>(peerIdIpMap);
				mapToBroadcast.put(MAC_ADDRESS,groupOwnerAddress);
				IMessage message = createMapMessage(mapToBroadcast, IMessage.BROADCAST_ADDRESS);
				sendBroadcast(message);
				mapSent = true;
				// Now start estimation phase
				startEstimator();
			}
		} else {
			// If I'm not the group owner and I'm here, I received the map. So I can start estimation phase
			__mock_provider.updateLocation();
			startEstimator();
		}

		if(mapToBroadcast == null) mapToBroadcast = peerIdIpMap;
		String s = "Map updated! \n";
		for(String k : mapToBroadcast.keySet()){
			s += k + "  " + mapToBroadcast.get(k)+"\n";
		}
		Log.d(TAG, this.getClass().getSimpleName()+": Message received\n"+s);
	}

	/**
	 * Starts range estimator service
	 * 
	 */
	private void startEstimator() {
		Log.d(TAG, this.getClass().getSimpleName()+": Bindo il servizio di Range Estimation");
		Intent estimationService = new Intent(context, FastBroadcastService.class);
		estimationService.putStringArrayListExtra("devices",new ArrayList<String>(peerIdIpMap.values()));
		context.startService(estimationService);
		context.bindService(estimationService, fastBroadcastServiceConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Creates Map message for all the peers
	 * 
	 * @param map
	 * @param recipient
	 * @return
	 */
	private IMessage createMapMessage(Map<String,String> map, String recipient){
		IMessage message = MessageBuilder.getInstance().getMessage(IMessage.CLIENT_MAP_MESSAGE_TYPE,recipient);
		int i = 1;
		for(String k : map.keySet()){
			message.addContent(k, IMessage.concatContent(map.get(k), ""+i));
			i++;
		}
		message.prepare();
		return message;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public Map<String,String> getPeersMap(){
		return peerIdIpMap;
	}

	/**
	 * Starts peers discovering
	 * 
	 */
	public void discoverPeers(){
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
			public void onSuccess() {
				Log.d(TAG, this.getClass().getSimpleName()+": Discover Peers onSuccess called");
			}

			public void onFailure(int reasonCode) {
				Log.d(TAG, this.getClass().getSimpleName()+": Discover Peers ERROR: "+reasonCode);
			}
		});
	}

	/**
	 * Disconnects the peer
	 * 
	 */
	public void disconnect() {
		manager.removeGroup(channel, new ActionListener(){
			public void onSuccess() {
				Log.d(TAG, this.getClass().getSimpleName()+": Group removed");
			}
			public void onFailure(int reason) {
				Log.d(TAG, this.getClass().getSimpleName()+": Failed to remove group, reason = "+reason);
			}
		});
		context.unbindService(dataReceiverServiceConnection);
		if(isServiceBinded) {
			context.unbindService(serviceConn);
			isServiceBinded = false;
			Log.d(TAG, this.getClass().getSimpleName()+": location service unbound");
		}
		dataInterface.unregisterHandler(collectionHandler);
		if(fastBroadcastService != null) context.unbindService(fastBroadcastServiceConnection);

	}

	/**
	 * Calls connect for each device found
	 * 
	 */
	public void connectToAll() {
		for(WifiP2pDevice device : peers){
			connect(device);
		}
	}

	/**
	 * Utility function used to post string messages
	 * @param string
	 */
	private void showToast(String string) {
		Message msg = new Message();
		msg.obj = string;
		msg.what = GuiHandlerInterface.SHOW_TOAST_MSG;
		guiHandler.sendMessage(msg);
	}

	/**
	 * Send the given message to all
	 * 
	 * @param message
	 */
	public void sendBroadcast(IMessage message) {
		if(peerIdIpMap != null)
			TransmissionManagerFactory.getInstance().getTransmissionManager().send(
					new ArrayList<String>(peerIdIpMap.values()),
					message);
	}

	@Override
	public boolean isGroupOwner() {
		return isGroupOwner;
	}

	@Override
	public String getGroupOwnerAddress() {
		return groupOwnerAddress;
	}

	@Override
	public void helloMessageArrived(IMessage message){
		fastBroadcastService.helloMessageReceived(message);
	}

	@Override
	public String getDeviceId() {
		return MAC_ADDRESS;
	}

}
