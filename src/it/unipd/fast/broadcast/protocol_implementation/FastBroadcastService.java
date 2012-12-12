package it.unipd.fast.broadcast.protocol_implementation;

import it.unipd.fast.broadcast.helper.LogPrinter;
import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.ITranmissionManager;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * An implementation of fast broadcast estimation phase.
 * 
 * @author Moreno Ambrosin
 *
 */
public class FastBroadcastService extends Service implements ICommunicationHandler{
	
	private String TAG = "it.unipd.fast.broadcast";
	
	/**
	 * Static field which indicates the simulated actual range of each device
	 * This value is used to filter messages received from a "distance" > ACTUAL_MAX_RANGE
	 */
	private static final int ACTUAL_MAX_RANGE = 600;
	
	/******************************************* DECLARATIONS ******************************************/
	
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
	 */
	private class HelloMessageSender extends TimerTask {
		
		private Random randomGenerator = new Random();
		
		@Override
		public void run(){
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
			// After waiting a random time check whether another hello message arrived,
			// and if not, sends an hello message
			if(!helloMessageArrived){
				sendHelloMessage();
				Log.d(TAG,this.getClass().getSimpleName()+" : Sent Hello message to all after " +
						""+(TURN_DURATION+randomTime)+" msec");
			}else{
				Log.d(TAG,this.getClass().getSimpleName()+" : Hello Message was already sent!!");
			}
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
		}
		
		@Override
		public void run() {
			while(true){
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
					
					// calculate contention window
					int contentionWindow = (int)Math.floor(((Math.abs(maxRange-distance)/maxRange) * (CwMax-CwMin))+CwMin); 
	
					Log.d(TAG,"Contention Window = "+contentionWindow);
					
					// wait for a random time... 
					synchronized (this) {
						try {
							long rnd = randomGenerator.nextInt(contentionWindow);
							Log.e(TAG,"BroadcastPhase: sleeping for "+rnd+" ms");
							Thread.sleep(rnd);
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
								IMessage.BROADCAST_ADDRESS);
						newMessage.addContent(IMessage.SENDER_LATITUDE_KEY,currentLocation.getLatitude()+"");
						newMessage.addContent(IMessage.SENDER_LONGITUDE_KEY,currentLocation.getLongitude()+"");
						newMessage.addContent(IMessage.SENDER_RANGE_KEY,Math.max(lmbr,cmbr)+"");
						newMessage.addContent(IMessage.SENDER_DIRECTION_KEY,currentLocation.getBearing()+"");
						newMessage.addContent(IMessage.MESSAGE_HOP_KEY, ""+hopCount);
						newMessage.prepare();
						LogPrinter.getInstance().writeTimedLine("no messages arrived while sleeping. Forwarding message \n\tHop numbers: "+hopCount+".\n\t Size "+messageQueue.size());
						transmissionManager.send(devices, newMessage);
						
						Log.e(TAG,"MESSAGE FORWARDED!!");
						
						if(handler != null)
							handler.doOnForwarded();
					}else{
						LogPrinter.getInstance().writeTimedLine("alert already brodcasted by someone else, discarded. Size "+messageQueue.size());
						Log.e(TAG,"Double Alert, message discarded");
						/*IMessage temp = (IMessage) (messageQueue.toArray())[messageQueue.size()-1];
						messageQueue.clear();
						try {
							messageQueue.put(temp);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
						// At least another message arrived
						if(receivedFromBack(direction, latitude, longitude)){
							// Someone else forwarded it already
							Log.e(TAG,"MESSAGE RECEIVED FROM BACK!!");
							if(handler != null)
								handler.doOnForwarded();
						}
						Log.e(TAG,"MESSAGE NOT FORWARDED!!");
					}
				}
			}
			
		}
	}
	
	/**
	 * Binder to get service interface
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public class FastBroadcastServiceBinder extends Binder {
		
		public ICommunicationHandler getService() {
			return FastBroadcastService.this;
		}
	}
	
	/**
	 * list of all the devices in the network
	 */
	private List<String> devices;
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
	 * Timer for Hello message scheduling
	 */
	private Timer scheduler;
	
	/**
	 * Error used to determine if two devices are moving on the same direction
	 * 
	 */
	private final float ERROR = 1f;
	
	/**
	 * Trasmission manager used to send out messages
	 */
	private ITranmissionManager transmissionManager = TransmissionManagerFactory.getInstance().getTransmissionManager();
	
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
	 * 
	 */
	private List<Filter> filters = new ArrayList<Filter>();
	
	/********************************************** METHODS ********************************************/
	
	/**
	 * Sends hello message to all the devices
	 */
	protected void sendHelloMessage(){
		IMessage helloMessage = MessageBuilder.getInstance().getMessage(
				IMessage.HELLO_MESSAGE_TYPE,
				IMessage.BROADCAST_ADDRESS);
		
		helloMessage.addContent(IMessage.SENDER_LATITUDE_KEY,currentLocation.getLatitude()+"");
		helloMessage.addContent(IMessage.SENDER_LONGITUDE_KEY,currentLocation.getLongitude()+"");
		// Add sender range estimation
		helloMessage.addContent(IMessage.SENDER_RANGE_KEY,Math.max(lmfr, cmfr)+"");
		// Add the bearing
		helloMessage.addContent(IMessage.SENDER_DIRECTION_KEY,currentLocation.getBearing()+"");
		
		helloMessage.prepare();
		transmissionManager.send(devices, helloMessage);
	}
	
	@Override
	public synchronized void setHelloMessageArrived(boolean arrived){
		this.helloMessageArrived = arrived;
	}
	
	@Override
	public void helloMessageReceived(final IMessage message){
		setHelloMessageArrived(true);
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
		if(scheduler != null){
			scheduler.cancel();
			scheduler.purge();
		}
		if(messageForwarder != null){
			messageForwarder.interrupt();
		}
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		this.devices = intent.getStringArrayListExtra("devices");
		// Start scheduling on service binding
		scheduler.schedule(new HelloMessageSender(),TURN_DURATION,TURN_DURATION);
		return new FastBroadcastServiceBinder();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		stopExecuting();
		return super.onUnbind(intent);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG,this.getClass().getSimpleName()+" : Estimator Service is Up");
		// On service creation, starts hello message sender Scheduler
		// creating a timer to schedule hello message sending
		scheduler = new Timer();
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
				if(distance > ACTUAL_MAX_RANGE) return false;
				return true;
			}
		});
		
	}
	
	private static int CwMax = 10000;
	private static int CwMin = 4000;
	
	@Override
	public void handleMessage(final IMessage message){
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
	public void setCurrentLocation(Location location) {
		currentLocation = location;
	}
	
	OnForwardedHandler handler;
	
	@Override
	public void setOnforwardedHadler(OnForwardedHandler handler) {
		this.handler = handler;
	}

	@Override
	public double getEstimatedRange() {
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
}
