package it.unipd.fast.broadcast.wifi_connection;
//package it.unipd.fast.broadcast.wifi_connection;
//
//import it.unipd.fast.broadcast.GuiHandlerInterface;
//import it.unipd.fast.broadcast.wifi_connection.receiver.FastBroadcastReceiver;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.IntentFilter;
//import android.net.wifi.WpsInfo;
//import android.net.wifi.p2p.WifiP2pConfig;
//import android.net.wifi.p2p.WifiP2pDevice;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.net.wifi.p2p.WifiP2pManager.ActionListener;
//import android.net.wifi.p2p.WifiP2pManager.Channel;
//import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
//import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//
///**
// * Controller used to manage the connection establishment
// * 
// * @author Moreno Ambrosin
// *
// */
//public class WiFiConnectionController {
//	protected final String TAG = "it.unipd.fast.broadcast";
//
//	Handler guiHandler;
//
//	private WifiP2pManager manager;
//	private Channel channel;
//	private BroadcastReceiver receiver;
//	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
//	private Map<String,String> peer_id_ip_map;
//	private Context context;
//	private IntentFilter intent_filter;
//
//
//	/**
//	 * Instance of DeviceDataReceiver class, used by the device as a channel to 
//	 * receive incoming messages
//	 * 
//	 */
//	private DeviceDataReceiver device_data_receiver = new IDataCollectionHandler(){
//
//
//		public void onDataCollected(String message, String host_ip) {
//
//			// Obtain message type from the xml source
//			int message_type = Integer.valueOf(XMLParser.estractTagAttributeFromXMLDoc(message, "message", "type"));
//
//			Log.d(TAG, this.getClass().getSimpleName()+": Message received, of type = "+message_type);
//
//			// Switching on message type
//			switch(message_type){
//
//			// In case of Hello message 
//			case HELLO_MESSAGE_TYPE : 
//				Map<String,String> peer_data = new HashMap<String, String>();
//				String client_id_address = XMLParser.estractTagFromXMLDoc(message, "client_id");
//				peer_data.put(client_id_address,host_ip);
//				//handler.onDataCollected(peer_data);
//				setPeersIdIPmap(peer_data);
//				break;
//
//				// In case of message MAP, for client addresses distribution
//			case CLIENT_MAP_MESSAGE_TYPE :
//				Map<String,String> all_peer_data = new HashMap<String, String>();
//				all_peer_data.putAll(XMLParser.extractDevicesInfo(message));
//				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuta lista");
//				//handler.onDataCollected(all_peer_data);
//				setPeersIdIPmap(all_peer_data);
//				break;
//
//			case ALERT_MESSAGE_TYPE :
//				Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto : "+message);
//				break;
//
//			default : 
//				Log.d(TAG, this.getClass().getSimpleName()+": Unknown message type "+message_type+", discarded.");
//				// TODO : unknown message type error handling
//			}
//		}
//
//
//		public void onError(String error) {
//			showToast(error);
//		}
//
//	});
//
//
//	/**
//	 * PeerListListener implementation
//	 * 
//	 */
//	private PeerListListener peer_listener = new PeerListListener() {
//		public void onPeersAvailable(WifiP2pDeviceList peers_list) {
//			Log.d(TAG, this.getClass().getSimpleName()+": Peers Added to the List");
//			peers.clear();
//			peers.addAll(peers_list.getDeviceList());
//			Message msg = new Message();
//			msg.obj = peers;
//			msg.what = GuiHandlerInterface.UPDATE_PEERS;
//			guiHandler.sendMessage(msg);
//		}
//
//	};
//
//	/**
//	 * ConnectionInfoListener implementation, used to react on connection available
//	 * 
//	 */
//	private ConnectionInfoListener connection_info_listener = new ConnectionInfoListener(){
//
//		public void onConnectionInfoAvailable(final WifiP2pInfo info) {
//			// Create a Thread to execute potentially blocking operations.
//			new Thread(){
//				public void run(){
//					// group owner InetAddress from WifiP2pInfo.
//					InetAddress groupOwnerAddress = info.groupOwnerAddress;
//
//					// After the group negotiation, we can determine the group owner.
//					Log.d(TAG, this.getClass().getSimpleName()+": GroupFormed = "+info.groupFormed);
//					Log.d(TAG, this.getClass().getSimpleName()+": isGroupOwner = "+info.isGroupOwner);
//					Log.d(TAG, this.getClass().getSimpleName()+": Address = "+groupOwnerAddress.getCanonicalHostName());
//
//					// Check if the group is formed
//					if (info.groupFormed){
//						// Depending on being the group owner or not, there are different tasks
//						// to do at this point
//						if(info.isGroupOwner){
//							// Do nothing, simply waits for other devices to send Hello messages
//							showToast("I'am the group owner");
//
//							// Waits 10 sec for the handshake, after it send map to all
//							new Timer().schedule(new TimerTask() {
//
//								@Override
//								public void run() {
//									try{
//										Log.d(TAG, this.getClass().getSimpleName()+": Sending map to all!!");
//										String message = "<message type='1'>";
//										for(String key : peer_id_ip_map.keySet()){
//											message += "" +
//													"<device>" +
//													"<client_id>"+key+"</client_id>" +
//													"<client_IP>"+peer_id_ip_map.get(key)+"</client_IP>" +
//													"</device>";
//										}
//										message += "</message>";
//										for(String key : peer_id_ip_map.keySet()){
//											send(
//													peer_id_ip_map.get(key),	// GroupOwner IP
//													8888, 						// Port on witch Hosts are waiting
//													message);
//										}
//									}catch(Exception e){
//
//									}
//								}
//							},10000);
//
//						}else{
//							// Not group Owner, so send an Hello Message to the GroupOwner
//							final WifiP2pInfo info2 = info;
//							Log.d(TAG, this.getClass().getSimpleName()+": Sending info!!");
//
//							new Thread(){
//								public void run() {
//									try{
//										showToast("Sending my address to Group owner");
//										send(
//												info2.groupOwnerAddress.getCanonicalHostName(),	// GroupOwner IP
//												8888, 											// Port on witch Hello Service is wating
//												"<message type='0'>" +							// XML Hello message
//												"<client_id>"+Settings.Secure.ANDROID_ID+"</client_id>"+
//												"</message>");
//									}catch(Exception e){
//										e.printStackTrace();
//									}
//								};
//							}.start();
//
//							Log.d(TAG, this.getClass().getSimpleName()+": I am NOT the group owner");
//						}
//					}
//				}
//			}.start();
//		};
//
//	};
//
//	/**
//	 * Constructor
//	 * 
//	 * @param context
//	 */
//	public WiFiConnectionController(Context context, GuiHandlerInterface guiHandlerInterface) {
//		this.context = context;
//		this.guiHandler = guiHandlerInterface.getGuiHandler();
//
//		// manager and channel initialization
//		manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
//		channel = manager.initialize(context, context.getMainLooper(), null);
//
//		// Register intent filter to receive specific intents
//		intent_filter = new IntentFilter();
//		intent_filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//		intent_filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//		intent_filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//		intent_filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//
//		// As last operation, run the data receiver, to receive and process messages
//		if(!device_data_receiver.isAlive()) device_data_receiver.start();
//	}
//
//	/**
//	 * Manages connection to the given device
//	 * 
//	 * @param device
//	 */
//	public void connect(WifiP2pDevice device){
//		WifiP2pConfig config = new WifiP2pConfig();
//		config.deviceAddress = device.deviceAddress;
//		config.wps.setup = WpsInfo.PBC;
//		config.groupOwnerIntent = 15;
//		manager.connect(channel,config, new ActionListener() {
//
//			public void onSuccess() {
//				showToast("Richiesta di connessione effettuata");
//			}
//
//			public void onFailure(int reason) {
//				showToast("Impossibil Connettersi reason = "+reason);
//			}
//		});
//
//
//	}
//
//	/**
//	 * Tell whether register/unregister FastBroadcastReceiver
//	 * 
//	 * @param registered
//	 */
//	public void setFastBroadCastReceiverRegistered(boolean registered){
//		if(registered) {
//			receiver = new FastBroadcastReceiver(manager, channel,peer_listener,this.connection_info_listener);
//			context.registerReceiver(receiver, intent_filter);
//		}else{
//			context.unregisterReceiver(receiver);
//		}
//	}
//	
//	/**
//	 * Registers BroadcastReceiver and requests DataReceiverService
//	 */
//	public void aquireResources() {
//		receiver = new FastBroadcastReceiver(manager, channel,peer_listener,this.connection_info_listener);
//		context.registerReceiver(receiver, intent_filter);
//	}
//	
//	/**
//	 * Unregisters BroadcastReceiver and releases DataReceiverService
//	 */
//	public void releaseResources() {
//		context.unregisterReceiver(receiver);
//	}
//
//	/**
//	 * Setter method for the map
//	 * 
//	 * @param map
//	 */
//	protected void setPeersIdIPmap(Map<String,String> map){
//		if(peer_id_ip_map == null) peer_id_ip_map = map;
//		else peer_id_ip_map.putAll(map);
//		String s = "Map updated! \n";
//		for(String k : peer_id_ip_map.keySet()){
//			s += k + "  " + peer_id_ip_map.get(k)+"\n";
//		}
//		showToast(s);
//	}
//
//	public Map<String,String> getPeersMap(){
//		return peer_id_ip_map;
//	}
//
//	/**
//	 * Starts peers discovering
//	 * 
//	 */
//	public void discoverPeers(){
//		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//			public void onSuccess() {
//				Log.d(TAG, this.getClass().getSimpleName()+": Discover Peers onSuccess called");
//			}
//
//			public void onFailure(int reasonCode) {
//				Log.d(TAG, this.getClass().getSimpleName()+": Discover Peers ERROR: "+reasonCode);
//			}
//		});
//	}
//
//	/**
//	 * Sends string message to a host at a specific port via Socket
//	 * 
//	 * @param host
//	 * @param port
//	 * @param msg
//	 * @throws Exception
//	 */
//	public void send(String host,int port,String msg) throws Exception{
//		Socket socket = new Socket();
//		byte buf[]  = new byte[1024];
//		int len = buf.length;
//		try {
//			/**
//			 * Create a client socket with the host,
//			 * port, and timeout information.
//			 */
//			socket.bind(null);
//			socket.connect((new InetSocketAddress(host, port)));
//
//			OutputStream outputStream = socket.getOutputStream();
//			InputStream inputStream = new ByteArrayInputStream(msg.getBytes("UTF-8"));
//			while ((len = inputStream.read(buf)) != -1) {
//				outputStream.write(buf, 0, len);
//			}
//			outputStream.close();
//			inputStream.close();
//			Log.d(TAG, this.getClass().getSimpleName()+": Data Sent to "+host+" via TCP");
//		} catch (Exception e) {
//			Log.d(TAG, this.getClass().getSimpleName()+": "+e.getMessage());
//			throw e;
//		}
//
//
//		/**
//		 * Clean up any open sockets when done
//		 * transferring or if an exception occurred.
//		 */
//		finally {
//			if (socket != null) {
//				if (socket.isConnected()) {
//					try {
//						socket.close();
//					} catch (IOException e) {
//						throw e;
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * Disconnects the peer
//	 * 
//	 */
//	public void disconnect() {
//		manager.removeGroup(channel, new ActionListener(){
//			public void onSuccess() {
//				Log.d(TAG, this.getClass().getSimpleName()+": Group removed");
//			}
//			public void onFailure(int reason) {
//				Log.d(TAG, WiFiConnectionController.class.getSimpleName()+": Failed to remove group, reason = "+reason);
//			}
//		});
//		device_data_receiver.stopDataReceiver();
//
//	}
//
//	/**
//	 * Method used to send broadcast an XML message
//	 * 
//	 * @param msg
//	 */
//	public void sendBroadcast(final String msg) {
//		if(peer_id_ip_map != null && !peer_id_ip_map.isEmpty()){
//			new Thread(){
//				public void run(){
//					for(String peer_id : peer_id_ip_map.keySet()){
//						try {
//							send(peer_id_ip_map.get(peer_id),8888,msg);
//						} catch (Exception e) {
//							e.printStackTrace();
//							Log.d(TAG,this.getClass().getSimpleName()+": Impossibile inviare broadcast: "+e.getMessage());
//						}
//					}
//				}
//			}.start();
//
//		}
//	}
//
//	/**
//	 * Calls connect for each device found
//	 * 
//	 */
//	public void connectToAll() {
//		for(WifiP2pDevice device : peers){
//			connect(device);
//		}
//	}
//
//	/**
//	 * Utility funtion unsed to post string messages
//	 * @param string
//	 */
//	private void showToast(String string) {
//		Message msg = new Message();
//		msg.obj = string;
//		msg.what = GuiHandlerInterface.SHOW_TOAST_MSG;
//		guiHandler.sendMessage(msg);
//	}
//}
