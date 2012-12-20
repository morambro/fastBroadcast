package it.unipd.testbase.protocol;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.location.LocationChangedEvent;
import it.unipd.testbase.eventdispatcher.event.location.UpdateLocationEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.AlertMessageArrivedEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.EstimationPhaseStartEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.HelloMessageArrivedEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.SendBroadcastMessageEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.StopSimulationEvent;
import it.unipd.testbase.helper.LogPrinter;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;
import it.unipd.testbase.wificonnection.transmissionmanager.ITranmissionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import android.location.Location;
import android.os.Binder;
import android.util.Log;

/**
 * An implementation of fast broadcast estimation phase.
 * 
 * @author Moreno Ambrosin
 *
 */
public class FastBroadcastService implements IFastBroadcastComponent{
	
	private String TAG = "it.unipd.testbase";
	
	/**
	 * Binder to get service interface
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public class FastBroadcastServiceBinder extends Binder {
		
		public IFastBroadcastComponent getService() {
			return FastBroadcastService.this;
		}
	}
	
	/**
	 * Current-turn Maximum Front Range
	 */
	private double cmfr = 100;
	/**
	 * Current-turn Maximum Back Range
	 */
	private double cmbr = 100;
	/**
	 * Last-turn Maximum Front Range
	 */
	private double lmfr = 300;
	/**
	 * Last-turn Maximum Back Range
	 */
	private double lmbr = 300;
	
	/**
	 * Error used to determine if two devices are moving on the same direction
	 * 
	 */
	private final float ERROR = 1f;
	
	/**
	 * Tells whether another hello message arrived
	 */
	private Boolean helloMessageArrived = false;
	
	/**
	 * Message forwarder thread used to send Alert messages back
	 * 
	 */
	private MessageForwarder messageForwarder = new MessageForwarder();
	
	/**
	 * Filters
	 */
	private List<Filter> filters = new ArrayList<Filter>();
	
	/**
	 * Static field which indicates the simulated actual range of each device
	 * This value is used to filter messages received from a "distance" > ACTUAL_MAX_RANGE
	 */
	private static final int ACTUAL_MAX_RANGE = 900;
	
	/**
	 * Slot size in milliseconds
	 */
	private static final int SLOT_SIZE = 100;
	
	/**
	 * Turn duration in milliseconds
	 */
	public static final int TURN_DURATION = 100000;
	
	/**
	 * Contention window bouds
	 */
	private static int CwMax = 1024;
	private static int CwMin = 31;
	
	private HelloMessageSender helloMessageSender;
	/****************************************************** DECLARATIONS ***************************************************/
	
	/**
	 * Interface used to define messages' filters 
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public interface Filter {
		/**
		 * Returns message if is not filtered
		 * 
		 * @param message
		 * @return
		 */
		boolean filter(IMessage message);
	}
	
	/**
	 * Task scheduled at a fixed TURN_DURATION time, which sends out an hello message 
	 * to perform Range estimation
	 * 
	 * @author Moreno Ambrosin
	 */
	private class HelloMessageSender extends Thread {
		
		private Random randomGenerator = new Random();
		private boolean keepRunning = true;
		
		@Override
		public void run(){
			while(keepRunning){
				try {
					Thread.sleep(TURN_DURATION);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int randomTime = 0;
				// Store previous current range into last current range
				lmbr = cmbr;
				lmfr = cmfr;
				
				synchronized (this) {
					helloMessageArrived = false;
					randomTime = randomGenerator.nextInt(TURN_DURATION);
					try{
						Log.d(TAG, "Going to sleep for "+randomTime+" ms");
						Thread.sleep(randomTime);
					}catch(InterruptedException ex){
						stopExecuting();
						ex.printStackTrace();
					}
				}
				// After waiting a random time check whether another hello message arrived, and if not, sends an hello message
				if(!helloMessageArrived){
					this.sendHelloMessage();
					Log.d(TAG,this.getClass().getSimpleName()+" : Sent Hello message to all after " +
							""+(TURN_DURATION+randomTime)+" msec");
				}else{
					Log.d(TAG,this.getClass().getSimpleName()+" : Hello Message was already sent!!");
				}
			}
		}
		
		/**
		 * Stops Hello Message Sender Execution
		 * 
		 */
		public void stopExecuting(){
			keepRunning = false;
		}
		
		/**
		 * Sends hello message broadcast
		 */
		private void sendHelloMessage(){
			IMessage helloMessage = MessageBuilder.getInstance().getMessage(
					IMessage.HELLO_MESSAGE_TYPE,
					ITranmissionManager.BROADCAST_ADDRESS);
			
			helloMessage.addContent(IMessage.SENDER_LATITUDE_KEY,currentLocation.getLatitude()+"");
			helloMessage.addContent(IMessage.SENDER_LONGITUDE_KEY,currentLocation.getLongitude()+"");
			// Add sender range estimation
			helloMessage.addContent(IMessage.SENDER_RANGE_KEY,Math.max(lmfr, cmfr)+"");
			// Add the bearing
			helloMessage.addContent(IMessage.SENDER_DIRECTION_KEY,currentLocation.getBearing()+"");
			
			helloMessage.prepare();
			
			// Send the hello message broadcast
			EventDispatcher.getInstance().triggerEvent(new SendBroadcastMessageEvent(helloMessage));
		}
	}
	
	/**
	 * Thread used to eventually forward an ALERT message
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	private class MessageForwarder extends Thread {
		
		/*********************************** DECLARATIONS *************************************/
		private ArrayBlockingQueue<IMessage> messageQueue = new ArrayBlockingQueue<IMessage>(30);
		private Random randomGenerator = new Random();
		private Object synchPoint = new Object();
		private boolean keepRunning = true;
		/************************************* METHODS ****************************************/
		/**
		 * Method used to send a message to the message forwarder
		 * 
		 * @param message
		 * @throws InterruptedException
		 */
		public void put(IMessage message) throws InterruptedException{
			LogPrinter.getInstance().writeTimedLine("alert message put into queue. Size "+(messageQueue.size()+1));
			messageQueue.put(message);
			// As soon as a new message arrived, notify the forwarder, to let him preceding without waiting 
			// for the entire random amount 
			synchronized (synchPoint) {
				synchPoint.notify();
			}
		}
		
		/**
		 * Stops Message Forwarder Execution
		 */
		public void stopExecuting(){
			keepRunning = false;
		}
		
		@Override
		public void run() {
			while(keepRunning){
				IMessage message = null;
				try {
					message = messageQueue.take();
					Map<String,String> content = message.getContent();
					int hopCount = Integer.valueOf(content.get(IMessage.MESSAGE_HOP_KEY));
					LogPrinter.getInstance().writeTimedLine("alert message taken from queue. Hop number: "+hopCount+". Size "+messageQueue.size());
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}

				if(message == null) return;
				boolean arrived = true;
				for(Filter filter : filters){
					arrived = arrived & (filter.filter(message));
				}
				if(!arrived){
					LogPrinter.getInstance().writeTimedLine("message discarded from queue, sender was too far away. Size "+messageQueue.size());
					Log.e(TAG,"MESSAGE SHOULDN'T HAVE BEEN ARRIVED SHIT!!");
				}else{
					Log.e(TAG,"ALERT MESSAGE ARRIVED!!");
					// now I am sure message != null
					Map<String,String> content = message.getContent();
					double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
					double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
					double maxRange 	= Double.valueOf(content.get(IMessage.SENDER_RANGE_KEY));
					float direction 	= Float.valueOf(content.get(IMessage.SENDER_DIRECTION_KEY));
					int hopCount = Integer.valueOf(content.get(IMessage.MESSAGE_HOP_KEY))+1;
					float distance = getDistance(latitude, longitude);
					
					Log.d(TAG,"Distance = "+distance);
					Log.d(TAG,"MaxRange = "+maxRange);
					
					int contentionWindow = CwMin;
					if(maxRange - distance > 0){
						// If the difference is negative, the device will use minimum contention window.
						contentionWindow = (int)Math.floor((((maxRange-distance)/maxRange) * (CwMax-CwMin))+CwMin); 
					}
	
					Log.d(TAG,"Contention Window = "+contentionWindow);
					
					// wait for a random time... 
					synchronized (synchPoint) {
						try {
							long rnd = randomGenerator.nextInt(contentionWindow*SLOT_SIZE);
							Log.e(TAG,"BroadcastPhase: sleeping for "+rnd+" ms");
							//Thread.sleep(rnd);
							// Waiting until:
							//    1) A message arrives, so stop waiting and change position
							//    2) Time expired, and so forward the message
							synchPoint.wait(rnd);
						} catch (InterruptedException e) {
							e.printStackTrace();
							Thread.currentThread().interrupt();
						}
					}
					Log.e(TAG,"BroadcastPhase: waking up");
					if(!messageQueue.contains(message)){
						// No message arrived while I was asleep
						IMessage newMessage = MessageBuilder.getInstance().getMessage(
								message.getType(),
								ITranmissionManager.BROADCAST_ADDRESS);
						newMessage.addContent(IMessage.SENDER_LATITUDE_KEY,currentLocation.getLatitude()+"");
						newMessage.addContent(IMessage.SENDER_LONGITUDE_KEY,currentLocation.getLongitude()+"");
						newMessage.addContent(IMessage.SENDER_RANGE_KEY,Math.max(lmbr,cmbr)+"");
						newMessage.addContent(IMessage.SENDER_DIRECTION_KEY,currentLocation.getBearing()+"");
						newMessage.addContent(IMessage.MESSAGE_HOP_KEY, ""+hopCount);
						newMessage.prepare();
						LogPrinter.getInstance().writeTimedLine("no messages arrived while sleeping. Forwarding message \n\tHop numbers: "+hopCount+".\n\t Size "+messageQueue.size());
						// Send broadcast the message
						EventDispatcher.getInstance().triggerEvent(new SendBroadcastMessageEvent(newMessage));
						
						Log.e(TAG,"MESSAGE FORWARDED!!");
						
						EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
					}else{
						LogPrinter.getInstance().writeTimedLine("alert already brodcasted by someone else, discarded. Size "+messageQueue.size());
						Log.e(TAG,"Double Alert, message discarded");
						// At least another message arrived
						if(receivedFromBack(direction, latitude, longitude)){
							// Someone else forwarded it already
							Log.e(TAG,"MESSAGE RECEIVED FROM BACK!!");
							EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
						}
						Log.e(TAG,"MESSAGE NOT FORWARDED!!");
					}
				}
			}
			
		}
	}
	
	/************************************************** METHODS ***********************************************/
	
	protected synchronized void setHelloMessageArrived(boolean arrived){
		this.helloMessageArrived = arrived;
	}
	
	/**
	 * Performs updates necessary when a message arrives
	 * 
	 * @param message
	 */
	private void hanldeHelloMessage(final IMessage message){
		new Thread(){
			public void run() {
				Map<String,String> content = message.getContent();
				double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
				double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
				double max_range  	= Double.parseDouble(content.get(IMessage.SENDER_RANGE_KEY));
				// Retrieve the sender bearing
				float direction 		= Float.valueOf(content.get(IMessage.SENDER_DIRECTION_KEY));
				
				float[] results = new float[3];
				
				Location.distanceBetween(
						latitude, longitude, 
						currentLocation.getLatitude(), currentLocation.getLongitude(), 
						results);
				
				// If I'm in the same direction, check whether I'm in front of him or not
				if(areEquals(currentLocation.getBearing(),direction,ERROR)){
					if(receivedFromBack(direction,latitude,longitude)){
						// Received from back
						cmbr = Math.max(cmbr, Math.max(results[0], max_range));
						Log.d(TAG,"Distance = "+results[0]+"   cmbr = "+cmbr);
						Log.d(TAG,this.getClass().getSimpleName()+" : Sono davanti, aggiorno cmbr a = "+cmbr);
					}else{
						// Received from front
						cmfr = Math.max(cmfr, Math.max(results[0],max_range));
						Log.d(TAG,this.getClass().getSimpleName()+" : Sono dietro, aggiorno cmfr a = "+cmfr);
					}
				}else{
					// Different directions, ignore the message
					Log.d(TAG,this.getClass().getSimpleName()+" : Messaggio proveniente da direzione diversa, lo ignoro");
				}
			}
		}.start();
	}
	
	/**
	 * Tells if the direction are the same, within a certain error
	 * 
	 * @param myBearing
	 * @param direction
	 * @return
	 */
	private boolean areEquals(float myBearing,float direction,float error){
		return Math.abs(myBearing-direction) < error;
	}
	
	/**
	 * Tells if a received message comes from back or not 
	 * 
	 * @param direction
	 * @param senderLatitude
	 * @param senderLongitude
	 * 
	 * @return true if the sender is back, false otherwise
	 */
	private boolean receivedFromBack(float direction,double senderLatitude,	double senderLongitude){
		if(direction <= 180){
			if(direction == 90){
				if(senderLongitude < currentLocation.getLongitude()){
					return true;
				}
			}else if(direction < 90){
				// 1° quadrante
				if(senderLatitude < currentLocation.getLatitude()){
					// Sono davanti
					return true;
				}
			}else{
				// 4° quadrante
				if(senderLatitude < currentLocation.getLatitude()){
					// Sono davanti
					return true;
				}
			}
			
		} else {
			if(direction == 270){
				if(senderLongitude > currentLocation.getLongitude()){
					return true;
				}
			}else if(direction > 270){
				// 2° quadrante
				if(senderLatitude > currentLocation.getLatitude()){
					// Sono davanti
					return true;
				}
			}else{
				// 3° quadrante
				if(senderLatitude > currentLocation.getLatitude()){
					Log.e(TAG, "FastBroadcastService: terzo quadrante");
					// Sono davanti
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void stopExecuting(){
		// Stop hello message sender and message forwarder
		if(helloMessageSender != null){
			helloMessageSender.stopExecuting();
		}
		if(messageForwarder != null){
			messageForwarder.stopExecuting();
		}
	}
	
	private static FastBroadcastService instance = null;
	
	
	public static FastBroadcastService getInstance() {
		if(instance == null)
			instance = new FastBroadcastService();
		return instance;
	}

	public FastBroadcastService() {
		Log.d(TAG,this.getClass().getSimpleName()+" : Estimator Service is Up");
		// Start message forwarder
		if(messageForwarder == null) messageForwarder = new MessageForwarder();
		messageForwarder.start();
		filters.add(new Filter(){
			@Override
			public boolean filter(IMessage message) {
				Map<String,String> content = message.getContent();
				double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
				double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
				float distance = getDistance(latitude, longitude);
				// If distance is > actual maximum range, message shouldn't be heard...
				
				Log.d(TAG,FastBroadcastService.class.getSimpleName()+" : ACTUAL RANGE = "+ACTUAL_MAX_RANGE);
				Log.d(TAG,FastBroadcastService.class.getSimpleName()+" : ESTIMATED RANGE = "+distance);
				
				if(distance > ACTUAL_MAX_RANGE) return false;
				return true;
			}
		});
		register();
	}
	
	
	private void handleMessage(final IMessage message){
		// Using a new thread to prevent main thread interruption
		new Thread(){
			@Override
			public void run() {
				try {
					messageForwarder.put(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private Location currentLocation;

	
	@Override
	public double getEstimatedTrasmissionRange() {
		return Math.max(cmbr,lmbr);
	}
	
	/**
	 * Returns the distance between current position and given latitude/logitude
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	private float getDistance(double latitude, double longitude){
		float[] results = new float[3];
		Location.distanceBetween(
				currentLocation.getLatitude(),
				currentLocation.getLongitude(),
				latitude, 
				longitude,
				results);
		return results[0];
	}

	@Override
	public void handle(IEvent event) {
		if(event.getClass().equals(EstimationPhaseStartEvent.class)){
			Log.d(TAG,"TACO EL TIMER");
			helloMessageSender = new HelloMessageSender();
			helloMessageSender.start();
			return;
		}
		if(event.getClass().equals(HelloMessageArrivedEvent.class)){
			HelloMessageArrivedEvent ev = (HelloMessageArrivedEvent) event;
			this.setHelloMessageArrived(true);
			hanldeHelloMessage(ev.message);
			return;
		}
		if(event.getClass().equals(AlertMessageArrivedEvent.class)){
			AlertMessageArrivedEvent ev = (AlertMessageArrivedEvent) event;
			this.setHelloMessageArrived(true);
			handleMessage(ev.alertMessage);
			return;
		}
		if(event.getClass().equals(LocationChangedEvent.class)){
			LocationChangedEvent ev = (LocationChangedEvent) event;
			this.currentLocation = ev.location;
			return;
		}
		if(event.getClass().equals(StopSimulationEvent.class)){
			stopExecuting();
			return;
		}
	}

	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(EstimationPhaseStartEvent.class);
		events.add(HelloMessageArrivedEvent.class);
		events.add(LocationChangedEvent.class);
		events.add(AlertMessageArrivedEvent.class);
		events.add(StopSimulationEvent.class);
		EventDispatcher.getInstance().registerComponent(this, events);
	}
}
