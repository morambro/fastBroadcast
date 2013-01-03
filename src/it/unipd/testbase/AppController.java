package it.unipd.testbase;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.gui.UpdateGuiEvent;
import it.unipd.testbase.eventdispatcher.event.location.LocationChangedEvent;
import it.unipd.testbase.eventdispatcher.event.location.SetupProviderEvent;
import it.unipd.testbase.eventdispatcher.event.location.UpdateLocationEvent;
import it.unipd.testbase.eventdispatcher.event.message.BeginSetupEvent;
import it.unipd.testbase.eventdispatcher.event.message.CompleteFileIndexReceivedEvent;
import it.unipd.testbase.eventdispatcher.event.message.PartialFileIndexReceivedEvent;
import it.unipd.testbase.eventdispatcher.event.message.MessageReceivedEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.EstimationPhaseStartEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.SendBroadcastMessageEvent;
import it.unipd.testbase.helper.Log;
import it.unipd.testbase.helper.LogPrinter;
import it.unipd.testbase.location.Location;
import it.unipd.testbase.protocol.IFastBroadcastComponent;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;
import it.unipd.testbase.wificonnection.receiver.CollectionHandler;
import it.unipd.testbase.wificonnection.transmissionmanager.ITranmissionManager;
import it.unipd.testbase.wificonnection.transmissionmanager.TransmissionManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AppController implements IControllerComponent {

	/********************************************** DECLARATIONS *************************************************/

	protected final String TAG = "it.unipd.testbase";

	private static final int APPLICATION_RUN_ID = new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE);
	private static final int APPLICATION_SETUP_MAX = 5000;
	private static final int APPLICATION_SETUP_PAD = 1000;
	private static final int SETUP_SLEEP_TIME = new Random(System.currentTimeMillis()).nextInt(APPLICATION_SETUP_MAX);
	private static boolean master = false;
	private SetupConfigurator configurator = null;

	private Location currentLocation;
	private IDataCollectionHandler collectionHandler = new CollectionHandler();
	private IFastBroadcastComponent fastBroadcastService;

	/************************************************* INTERFACES/CLASSES ********************************************/

	/**
	 * Interface used to specify operation to do on data collected or when an error occurs
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public static interface IDataCollectionHandler {
		public void onDataCollected(IMessage message,String sender);
	}

	class SetupConfigurator {
		private boolean locked = false;
		private List<Integer> devices = new ArrayList<Integer>();

		public SetupConfigurator() {
			Log.d(TAG, "Configurator started");
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(APPLICATION_SETUP_MAX+APPLICATION_SETUP_PAD);
						locked = true;
						master = false;
						IMessage m = MessageBuilder.getInstance().getMessage(IMessage.COMPLETE_FILE_COUNTER_INDEX, ""+APPLICATION_RUN_ID);
						for(int i=0; i<devices.size(); ++i)
							//use i+1 because 0 is reserved for master
							m.addContent(""+devices.get(i), ""+(i+1));
						m.prepare();
						EventDispatcher.getInstance().triggerEvent(new SendBroadcastMessageEvent(m));
						EventDispatcher.getInstance().triggerEvent(new SetupProviderEvent(0, devices.size()+1));
						EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_UNLOCK, null));
						EventDispatcher.getInstance().triggerEvent(new EstimationPhaseStartEvent());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		public synchronized void addDevice(int device) {
			if(!devices.contains(device) && !locked) {
				devices.add(device);
				EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_ADD_PEER, null));
			}
		}

	}

	/******************************************************* METHODS ************************************************/


	public static int getApplicatonRunID() {
		return APPLICATION_RUN_ID;
	}

	public static int getApplicatonSetupMax() {
		return APPLICATION_SETUP_MAX;
	}			

	public static void setMaster() {
		master = true;
	}

	public boolean isMaster() {
		return master;
	}

	public AppController() {
		fastBroadcastService = (IFastBroadcastComponent) EventDispatcher.getInstance().requestComponent(IFastBroadcastComponent.class);
		LogPrinter.setup(""+APPLICATION_RUN_ID);
		register();
	}

	@Override
	public void sendAlert() {
		IMessage message = MessageBuilder.getInstance().getMessage(IMessage.ALERT_MESSAGE_TYPE, ""+APPLICATION_RUN_ID);
		message.addContent(IMessage.SENDER_LATITUDE_KEY, ""+currentLocation.getLatitude());
		message.addContent(IMessage.SENDER_LONGITUDE_KEY, ""+currentLocation.getLongitude());
		message.addContent(IMessage.SENDER_RANGE_KEY, ""+fastBroadcastService.getEstimatedTrasmissionRange());
		message.addContent(IMessage.SENDER_DIRECTION_KEY, ""+currentLocation.getBearing());
		message.addContent(IMessage.MESSAGE_HOP_KEY, "1");
		message.prepare();
		EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
		sendBroadcast(message);
	}

	@Override
	public void sendBroadcast(IMessage message) {
		TransmissionManagerFactory.getInstance().getTransmissionManager().send(ITranmissionManager.BROADCAST_ADDRESS ,message);
	}

	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(LocationChangedEvent.class);
		events.add(MessageReceivedEvent.class);
		events.add(BeginSetupEvent.class);
		events.add(SendBroadcastMessageEvent.class);
		events.add(PartialFileIndexReceivedEvent.class);
		events.add(CompleteFileIndexReceivedEvent.class);
		EventDispatcher.getInstance().registerComponent(this, events);
	}

	@Override
	public void handle(IEvent event) {
		if(event instanceof LocationChangedEvent){
			LocationChangedEvent ev = (LocationChangedEvent) event;
			this.currentLocation = ev.location;
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
		if(event instanceof BeginSetupEvent){
			if(isMaster())
				configurator = new SetupConfigurator();
			else
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(SETUP_SLEEP_TIME);
							IMessage message = MessageBuilder.getInstance().getMessage(IMessage.PARTIAL_FILE_COUNTER_INDEX, ""+APPLICATION_RUN_ID);
							message.prepare();
							EventDispatcher.getInstance().triggerEvent(new SendBroadcastMessageEvent(message));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			return;
		}
		if(event instanceof PartialFileIndexReceivedEvent) {
			if(isMaster()) {
				PartialFileIndexReceivedEvent ev = (PartialFileIndexReceivedEvent) event;
				configurator.addDevice(ev.deviceID);
			}
		}
		if(event instanceof CompleteFileIndexReceivedEvent) {
			CompleteFileIndexReceivedEvent ev = (CompleteFileIndexReceivedEvent) event;
			String i = ev.map.get(""+APPLICATION_RUN_ID);
			if(i==null) {
				Log.e(TAG, this.getClass().getSimpleName()+": Device not present in map!!");
				return;
			}
			int deviceNum = ev.map.size()+1;
			int index = Integer.valueOf(i);
			EventDispatcher.getInstance().triggerEvent(new SetupProviderEvent(index, deviceNum));
			EventDispatcher.getInstance().triggerEvent(new EstimationPhaseStartEvent());
			EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_UNLOCK, null));
			EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_ADD_PEER, new Integer(deviceNum)));
		}
	}

}
