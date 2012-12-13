package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.event.IEvent;
import it.unipd.fast.broadcast.event.connectioninfo.WiFiInfoCollectedEvent;
import it.unipd.fast.broadcast.event.location.LocationChangedEvent;
import it.unipd.fast.broadcast.event.location.SetupProviderEvent;
import it.unipd.fast.broadcast.event.location.UpdateLocationEvent;
import it.unipd.fast.broadcast.event.message.MessageReceivedEvent;
import it.unipd.fast.broadcast.event.protocol.EstimationPhaseStartEvent;
import it.unipd.fast.broadcast.event.protocol.HelloMessageArrivedEvent;
import it.unipd.fast.broadcast.event.protocol.SendBroadcastMessageEvent;
import it.unipd.fast.broadcast.protocol.FastBroadcastService;
import it.unipd.fast.broadcast.protocol.IFastBroadcastComponent;
import it.unipd.fast.broadcast.wificonnection.connectionmanager.ConnectionManagerFactory;
import it.unipd.fast.broadcast.wificonnection.connectionmanager.IConnectionInfoManager;
import it.unipd.fast.broadcast.wificonnection.message.IMessage;
import it.unipd.fast.broadcast.wificonnection.message.MessageBuilder;
import it.unipd.fast.broadcast.wificonnection.receiver.CollectionHandler;
import it.unipd.fast.broadcast.wificonnection.receiver.FastBroadcastReceiver;
import it.unipd.fast.broadcast.wificonnection.transmissionmanager.TransmissionManagerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AppController implements IControllerComponent {

	/********************************************** DECLARATIONS *************************************************/

	protected final String TAG = "it.unipd.fast.broadcast";
	public static String MAC_ADDRESS = null;

	private Handler guiHandler;
	private Location currentLocation;
	private IDataCollectionHandler collectionHandler = new CollectionHandler();
	private WifiP2pManager manager;
	private Channel channel;
	private BroadcastReceiver receiver;
	
	private SynchronizedDevicesList peers = new SynchronizedDevicesList();
	
	private Map<String,String> peerIdIpMap;
	private Context context;
	private IntentFilter broadcastReceiverIntentFilter;
	private boolean mapSent = false;

	private String groupOwnerAddress;
	private boolean isGroupOwner = false;

	private IFastBroadcastComponent fastBroadcastService;

	/************************************************* INTERFACES/CLASSES ********************************************/
	
	/**
	 * Interface used to specify operation to do on data collected or when an error occurs
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public static interface IDataCollectionHandler {
		public void setWiFiController(AppController controller);
		public void onDataCollected(IMessage message,String sender);
		public void onError(String error);
	}
	
	/**
	 * PeerListListener implementation
	 * 
	 */
	private PeerListListener peerListener = new PeerListListener() {

		public void onPeersAvailable(WifiP2pDeviceList peers_list) {

			// Adds only new devices
			for(WifiP2pDevice device : peers_list.getDeviceList()){
				peers.add(device);
			}
			// remove disappeared devices
			for(int i = 0; i < peers.size(); i++){
				WifiP2pDevice device = peers.get(i);
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
	 * Listener defined to receive and handle connection info events.
	 */
	private IConnectionInfoManager connectionInfoListener = ConnectionManagerFactory.getInstance().getConnectionManager();

	/**
	 * Wrapper class used to access list via synchronized methods
	 * 
	 * @author Fabio De Gaspari
	 *
	 */
	public class SynchronizedDevicesList {
		
		private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
		
		synchronized boolean remove(WifiP2pDevice device){
			return peers.remove(device);
		}
		
		synchronized int size() {
			return peers.size();
		}

		synchronized void add(WifiP2pDevice device){
			if(!peers.contains(device)){
				Log.d(TAG,this.getClass().getSimpleName() + "Aggiunto device");
				peers.add(device);
			}else{
				Log.d(TAG,this.getClass().getSimpleName() + "Device already in the list");
			}
		}
		
		synchronized WifiP2pDevice get(int index){
			return peers.get(index);
		}
	}
	
	/******************************************************* METHODS ************************************************/

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public AppController(Context context, GuiHandlerInterface guiHandlerInterface) {
		this.context = context;
		this.guiHandler = guiHandlerInterface.getGuiHandler();
		collectionHandler.setWiFiController(this);
		// manager and channel initialization
		manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(context, context.getMainLooper(), null);
		// Register intent filter to receive specific intents
		broadcastReceiverIntentFilter = new IntentFilter();
		broadcastReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		broadcastReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		broadcastReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		broadcastReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		// Setting static field which contains device MAC address
		MAC_ADDRESS = getDeviceMacAddress();
		Log.d(TAG, this.getClass().getSimpleName()+": il MAC address del dispositivo è = "+MAC_ADDRESS);
		
		register();
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

	@Override
	public void setFastBroadCastReceiverRegistered(boolean registered){
		if(registered) {
			receiver = new FastBroadcastReceiver(manager, channel,peerListener,connectionInfoListener);
			context.registerReceiver(receiver, broadcastReceiverIntentFilter);
		}else{
			context.unregisterReceiver(receiver);
		}
	}

	/**
	 * Registers BroadcastReceiver and requests DataReceiverService
	 */
	public void aquireResources() {
		receiver = new FastBroadcastReceiver(manager, channel,peerListener,this.connectionInfoListener);
		context.registerReceiver(receiver, broadcastReceiverIntentFilter);
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
	public synchronized void setPeersIdIPmap(Map<String,String> map){

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
			if(!mapSent && peerIdIpMap.keySet().size() >= peers.size()){
				//size()+1 because group owner is not included in this map (so the returned size() equals (device_number-1)
				fastBroadcastService = (IFastBroadcastComponent)EventDispatcher.getInstance().requestComponent(FastBroadcastService.class);
				EventDispatcher.getInstance().triggerEvent(new SetupProviderEvent(0, peerIdIpMap.size()+1));
				//MockLocationProvider.__set_static_couter(0, peerIdIpMap.size()+1);
				EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
				Log.d(TAG,this.getClass().getCanonicalName()+": Invio la mappa a tutti : \n");
				mapToBroadcast = new HashMap<String, String>(peerIdIpMap);
				mapToBroadcast.put(MAC_ADDRESS,groupOwnerAddress);
				IMessage message = createMapMessage(mapToBroadcast, IMessage.BROADCAST_ADDRESS);
				
				sendBroadcast(message);
				
				mapSent = true;
				// Now start fast broadcast
//				startFastBroadcastService();
				EventDispatcher.getInstance().triggerEvent(new EstimationPhaseStartEvent());
			}
		} else {
			// If I'm not the group owner and I'm here, I received the map. So I can start estimation phase
			fastBroadcastService = (IFastBroadcastComponent)EventDispatcher.getInstance().requestComponent(FastBroadcastService.class);
			EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
			//__mock_provider.updateLocation();
			EventDispatcher.getInstance().triggerEvent(new EstimationPhaseStartEvent());
		}

		if(mapToBroadcast == null) mapToBroadcast = peerIdIpMap;
		String s = "Map updated! \n";
		for(String k : mapToBroadcast.keySet()){
			s += k + "  " + mapToBroadcast.get(k)+"\n";
		}
		Log.d(TAG, this.getClass().getSimpleName()+": Message received\n"+s);
	}

//	/**
//	 * Starts range estimator service
//	 * 
//	 */
//	private void startFastBroadcastService() {
//		Log.d(TAG, this.getClass().getSimpleName()+": Bindo il servizio di Range Estimation");
//		Intent estimationService = new Intent(context, FastBroadcastService.class);
//		estimationService.putStringArrayListExtra("devices",new ArrayList<String>(peerIdIpMap.values()));
//		context.startService(estimationService);
//		context.bindService(estimationService, fastBroadcastServiceConnection, Context.BIND_AUTO_CREATE);
//	}

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
			if(k.equals(MAC_ADDRESS))
				message.addContent(k, IMessage.concatContent(map.get(k), ""+0));
			else
			{
				message.addContent(k, IMessage.concatContent(map.get(k), ""+i));
				i++;
			}
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

	@Override
	public void disconnect() {
		manager.removeGroup(channel, new ActionListener(){
			public void onSuccess() {
				Log.d(TAG, this.getClass().getSimpleName()+": Group removed");
			}
			public void onFailure(int reason) {
				Log.d(TAG, this.getClass().getSimpleName()+": Failed to remove group, reason = "+reason);
			}
		});
//		context.unbindService(dataReceiverServiceConnection);
//		if(isServiceBinded) {
//			context.unbindService(locationServiceConn);
//			isServiceBinded = false;
//			Log.d(TAG, this.getClass().getSimpleName()+": location service unbound");
//		}
//		dataInterface.unregisterHandler(collectionHandler);
//		if(fastBroadcastService != null) context.unbindService(fastBroadcastServiceConnection);

	}

	@Override
	public void connectToAll() {
		for(int i = 0; i < peers.size(); i++){
			connect(peers.get(i));
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
	
	@Override
	public void sendAlert() {
		IMessage message = MessageBuilder.getInstance().getMessage(IMessage.ALERT_MESSAGE_TYPE, IMessage.BROADCAST_ADDRESS);
		message.addContent(IMessage.SENDER_LATITUDE_KEY, ""+currentLocation.getLatitude());
		message.addContent(IMessage.SENDER_LONGITUDE_KEY, ""+currentLocation.getLongitude());
		message.addContent(IMessage.SENDER_RANGE_KEY, ""+fastBroadcastService.getEstimatedRange());
		message.addContent(IMessage.SENDER_DIRECTION_KEY, ""+currentLocation.getBearing());
		message.addContent(IMessage.MESSAGE_HOP_KEY, "1");
		message.prepare();
		sendBroadcast(message);
	}

	@Override
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
		EventDispatcher.getInstance().triggerEvent(new HelloMessageArrivedEvent(message));
	}

	@Override
	public void handleMessage(IMessage message){
		fastBroadcastService.handleMessage(message);
	}
	
	@Override
	public String getDeviceId() {
		return MAC_ADDRESS;
	}
	
	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(LocationChangedEvent.class);
		events.add(MessageReceivedEvent.class);
		events.add(WiFiInfoCollectedEvent.class);
		events.add(SendBroadcastMessageEvent.class);
		
		EventDispatcher.getInstance().registerComponent(this, events);
	}

	@Override
	public void handle(IEvent event) {
		if(event instanceof LocationChangedEvent){
			LocationChangedEvent ev = (LocationChangedEvent) event;
			this.currentLocation = ev.location;
			Log.d(TAG,this.getClass().getSimpleName()+" : fastBroadcast "+fastBroadcastService);
			fastBroadcastService.setCurrentLocation(ev.location);
			return;
		}
		if(event instanceof MessageReceivedEvent){
			MessageReceivedEvent ev = (MessageReceivedEvent) event;
			collectionHandler.onDataCollected(ev.message, ev.senderID);
			return;
		}
		if(event instanceof SendBroadcastMessageEvent){
			SendBroadcastMessageEvent ev = (SendBroadcastMessageEvent) event;
			sendBroadcast(ev.message);
			return;
		}
		if(event instanceof WiFiInfoCollectedEvent){
			WiFiInfoCollectedEvent ev = (WiFiInfoCollectedEvent) event;
			groupOwnerAddress 	= ev.wifiConnectionInfo.groupOwnerAddress.getCanonicalHostName();
			isGroupOwner 		= ev.wifiConnectionInfo.isGroupOwner;
			return;
		}
	}

}
