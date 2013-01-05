package it.unipd.testbase;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.SetupCompletedEvent;
import it.unipd.testbase.eventdispatcher.event.connectioninfo.WiFiInfoCollectedEvent;
import it.unipd.testbase.eventdispatcher.event.location.PositionsTerminatedEvent;
import it.unipd.testbase.eventdispatcher.event.location.SetupProviderEvent;
import it.unipd.testbase.eventdispatcher.event.location.UpdateLocationEvent;
import it.unipd.testbase.eventdispatcher.event.message.MessageReceivedEvent;
import it.unipd.testbase.eventdispatcher.event.message.SendUnicastMessageEvent;
import it.unipd.testbase.eventdispatcher.event.message.UpdateStatusEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.SendBroadcastMessageEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.SimulationStartEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.StopSimulationEvent;
import it.unipd.testbase.helper.DebugLogger;
import it.unipd.testbase.wificonnection.connectioninfomanager.ConnectionManagerFactory;
import it.unipd.testbase.wificonnection.connectioninfomanager.IConnectionInfoManager;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;
import it.unipd.testbase.wificonnection.receiver.CollectionHandler;
import it.unipd.testbase.wificonnection.receiver.WifiBroadcastReceiver;
import it.unipd.testbase.wificonnection.transmissionmanager.TransmissionManager;
import it.unipd.testbase.wificonnection.transmissionmanager.TransmissionManager.TransportSelectorFilter;
import it.unipd.testbase.wificonnection.transmissionmanager.sender.IPaketSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
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

public class AppController implements IControllerComponent {

	/********************************************** DECLARATIONS *************************************************/

	private DebugLogger logger = new DebugLogger(AppController.class);
	
	protected final String TAG = "it.unipd.testbase";
	public static String MAC_ADDRESS = null;

	private Handler guiHandler;
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
	private boolean keepUpdatingPeers = true;
	
	private boolean setupCompleted = false;

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
			if(!keepUpdatingPeers) {
				logger.d("Ignoring peers update");
				Collection<WifiP2pDevice> devs = peers_list.getDeviceList();
				for(int i = 0 ; i < peers.size(); i++){
					if(devs.contains(peers.get(i))){
						for(WifiP2pDevice d : devs){
							// Update peers in the list
							if(d.deviceAddress == peers.get(i).deviceAddress){
								peers.set(i,d);
							}
						}
					}
				}
			}else{
				peers = new SynchronizedDevicesList();
				// Adds all found devices
				for(WifiP2pDevice device : peers_list.getDeviceList()){
					peers.add(device);
				}
			}

			Message msg = new Message();
			msg.obj = peers;
			msg.what = GuiHandlerInterface.UPDATE_PEERS;
			guiHandler.sendMessage(msg);

			logger.d("Peers Added to the List");
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
				logger.d("Aggiunto device");
				peers.add(device);
			}else{
				logger.d("Device already in the list");
			}
		}
		
		synchronized WifiP2pDevice get(int index){
			return peers.get(index);
		}
		
		synchronized WifiP2pDevice set(int i, WifiP2pDevice device){
			return peers.set(i,device);
		}

	}
	
	/******************************************************* METHODS ************************************************/

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public AppController(Context context, GuiHandlerInterface guiHandlerInterface,TransportSelectorFilter filter) {
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
		logger.d("il MAC address del dispositivo è = "+MAC_ADDRESS);
		
		// Force creation of TransportManager
		TransmissionManager.getInstance().setFilter(filter);
		
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
				showToast("Impossibile Connettersi reason = "+reason);
			}
		});


	}

	@Override
	public void setFastBroadCastReceiverRegistered(boolean registered){
		if(registered) {
			receiver = new WifiBroadcastReceiver(manager, channel,peerListener,connectionInfoListener);
			context.registerReceiver(receiver, broadcastReceiverIntentFilter);
		}else{
			context.unregisterReceiver(receiver);
		}
	}

	/**
	 * Registers BroadcastReceiver and requests DataReceiverService
	 */
	public void aquireResources() {
		receiver = new WifiBroadcastReceiver(manager, channel,peerListener,this.connectionInfoListener);
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
	 * all the devices requestConnectionSent to his group the complete map of all the peers
	 * 
	 * @param map
	 */
	public synchronized void setPeersIdIPmap(Map<String,String> map){

		// If setup was already completed, just do nothing
		if (setupCompleted) return;
		
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
				
				setupCompleted = true;
				EventDispatcher.getInstance().triggerEvent(new SetupProviderEvent(0, peerIdIpMap.size()+1));
				EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
				mapToBroadcast = new HashMap<String, String>(peerIdIpMap);
				mapToBroadcast.put(MAC_ADDRESS,groupOwnerAddress);
				IMessage message = createMapMessage(mapToBroadcast, IPaketSender.BROADCAST_ADDRESS);
				logger.d("Invio la mappa a tutti : ");
				sendBroadcast(message);
				mapSent = true;
				// Now start simulation
				EventDispatcher.getInstance().triggerEvent(new SimulationStartEvent());
				EventDispatcher.getInstance().triggerEvent(new SetupCompletedEvent());
				EventDispatcher.getInstance().triggerEvent(
						new UpdateStatusEvent("Group owner setup completed")
				);
				
			}
		} else {
			// If I'm not the group owner and I'm here, I received the map, 
			// so I can start simulation
			setupCompleted = true;
			EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
			EventDispatcher.getInstance().triggerEvent(new SimulationStartEvent());
			EventDispatcher.getInstance().triggerEvent(new SetupCompletedEvent());
			EventDispatcher.getInstance().triggerEvent(new UpdateStatusEvent("Peer Setup completed"));
		}

		if(mapToBroadcast == null)
			mapToBroadcast = peerIdIpMap;
		String s = "Current MAP:\n";
		for(String k : mapToBroadcast.keySet()){
			s += k + "  " + mapToBroadcast.get(k)+"\n";
		}
		logger.d("Message received\n"+s);
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
			if(k.equals(MAC_ADDRESS)){
				message.addContent(k, IMessage.concatContent(map.get(k), ""+0));
			}else{
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

	@Override
	public void disconnect() {
		manager.removeGroup(channel, new ActionListener(){
			public void onSuccess() {
				logger.d("Group removed");
			}
			public void onFailure(int reason) {
				if(reason == 2){
					logger.d("Failed to remove group reason = ("+reason+")");
				}
			}
		});
		manager.cancelConnect(channel, new ActionListener(){
			@Override
			public void onFailure(int reason) {
				if(reason == 2){
					logger.d("Failed to cancel connection requests, reason = ("+reason+")");
				}
			}
			
			@Override
			public void onSuccess() {
				logger.d("Canceled connection requests");
			}
		});
	}

	@Override
	public void connectToAll() {
		for(int i = 0; i < peers.size(); i++){
			connect(peers.get(i));
		}
		keepUpdatingPeers = false;
	}

	/**
	 * Utility function used to post string messages
	 * @param string
	 */
	private void showToast(String string) {
		Message msg = new Message();
		msg.obj = string;
		msg.what = GuiHandlerInterface.SHOW_TOAST_MESSAGE;
		guiHandler.sendMessage(msg);
	}
	

	private void sendBroadcast(IMessage message) {
		TransmissionManager.getInstance().sendBroadcast(
				new ArrayList<String>(peerIdIpMap.values()), 
				message);
	}
	
	private void sendUnicast(String recipient, IMessage message) {
		TransmissionManager.getInstance().sendUnicast(recipient, message);
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
	public String getDeviceId() {
		return MAC_ADDRESS;
	}
	
	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(MessageReceivedEvent.class);
		events.add(WiFiInfoCollectedEvent.class);
		events.add(SendBroadcastMessageEvent.class);
		events.add(SendUnicastMessageEvent.class);
		events.add(PositionsTerminatedEvent.class);
		events.add(UpdateStatusEvent.class);
		EventDispatcher.getInstance().registerComponent(this, events);
	}

	@Override
	public void handle(IEvent event) {
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
		if(event instanceof SendUnicastMessageEvent){
			SendUnicastMessageEvent ev = (SendUnicastMessageEvent) event;
			sendUnicast(ev.recipient,ev.message);
			return;
		}
		if(event instanceof WiFiInfoCollectedEvent){
			WiFiInfoCollectedEvent ev = (WiFiInfoCollectedEvent) event;
			groupOwnerAddress 	= ev.wifiConnectionInfo.groupOwnerAddress.getCanonicalHostName();
			isGroupOwner 		= ev.wifiConnectionInfo.isGroupOwner;
			return;
		}
		if(event instanceof PositionsTerminatedEvent){
			EventDispatcher.getInstance().triggerEvent(new StopSimulationEvent(true,false));
			return;
		}
		if(event instanceof UpdateStatusEvent){
			UpdateStatusEvent ev = (UpdateStatusEvent) event;
			Message message = new Message();
			message.what 	= GuiHandlerInterface.PROGRESS_MESSAGE;
			message.obj 	= ev.status;
			guiHandler.sendMessage(message);
			return;
		}
	}

}
