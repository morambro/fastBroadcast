package it.unipd.fast.broadcast.protocol_implementation;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.ITranmissionManager;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.List;
import java.util.Map;
import java.util.Queue;
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
public class FastBroadcastHandler extends Service implements ICommunicationHandler{
	
	private String TAG = "it.unipd.fast.broadcast";
	
	/******************************************* DECLARATIONS ******************************************/
	
	/**
	 * Task scheduled at a fixed TURN_DURATION time, which sends out an hello message 
	 * to perform Range estimation
	 */
	private class HelloMessageSender extends TimerTask {
		
		private Random randomGenerator = new Random();
		
		@Override
		public void run(){
			int randomTime = 0;
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
		
		private ArrayBlockingQueue<IMessage> messageQueue = new ArrayBlockingQueue<IMessage>(30);
		
		private Random randomGenerator = new Random();
		private IMessage message;
		
		public MessageForwarder(IMessage message) {
			this.message = message;
		}

		@Override
		public void run() {
			
			while(true){
				IMessage message = null;
				try {
					message = messageQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(message == null) return;
				// now I am sure message != null

				Map<String,String> content = message.getContent();
				double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
				double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
				int maxRange 		= Integer.valueOf(content.get(IMessage.SENDER_RANGE_KEY));
				int distance 		= 0; //TODO : calculate distance...

				// float[] results = new float[3];
				// Location.distanceBetweenPlaces(latitude, longitude, lon2, lat2)

				// calculate contention window
				int contentionWindow = (int)Math.floor((((maxRange-distance)/maxRange) * (CwMax-CwMin))+CwMin); 

				// wait for a random time... 
				synchronized (this) {
					try {
						Thread.sleep(randomGenerator.nextInt(contentionWindow));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if(messageQueue.isEmpty()){
					// No message arrived while I was asleep
					// TODO : send Message!
				}else{
					// 				
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
			return FastBroadcastHandler.this;
		}
	}
	
	/**
	 * list of all the devices in the network
	 */
	private List<String> devices;
	/**
	 * Current-turn Maximum Front Range
	 */
	private double cmfr = 300;
	/**
	 * Current-turn Maximum Back Range
	 */
	private double cmbr = 300;
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
	 * 
	 */
	private Boolean alertMessageReceived = false;
	
	/********************************************** METHODS ********************************************/
	
	/**
	 * Sends hello message to all the devices
	 */
	protected void sendHelloMessage(){
		IMessage helloMessage = MessageBuilder.getInstance().getMessage(
				IMessage.HELLO_MESSAGE_TYPE,
				IMessage.BROADCAST_ADDRESS);
		
		// TODO: prendere la POSIZIONEEEE!!!!
		
		helloMessage.addContent(IMessage.SENDER_LATITUDE_KEY,"45.227009");
		helloMessage.addContent(IMessage.SENDER_LONGITUDE_KEY,"11.775048");
		// Add sender range estimation
		helloMessage.addContent(IMessage.SENDER_RANGE_KEY,Math.max(lmfr, cmfr)+"");
		// Add the bearing
		helloMessage.addContent(IMessage.HELLO_SENDER_DIRECTION_KEY,"45");
		
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
				int direction 		= Integer.parseInt(content.get(IMessage.HELLO_SENDER_DIRECTION_KEY));
				
				Location l = new Location(""); // TODO : Retrieve my location
				
				float[] results = new float[3];
				Location.distanceBetween(latitude, longitude, l.getLatitude(), l.getLongitude(), results);
				
				float myBearing = 45;//TODO : retrieve bearing from LocationProvider
				
				// If I'm in the same direction, check whether I'm in front of him or not
				if(areEquals(myBearing,direction,ERROR)){
					if(receivedFromBack(direction,l.getLatitude(),latitude)){
						// Received from back
						cmbr = Math.max(cmbr, Math.max(results[0], max_range));
					}else{
						// Received from front
						cmfr = Math.max(cmfr, Math.max(results[0],max_range));
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
	
	
	private boolean receivedFromBack(float direction,double latitude,double senderLatitude){
		if(direction > 0){
			if(!(senderLatitude > latitude)){
				// Sono davanti
				return true;
			}
		}else{
			if(senderLatitude > latitude){
				// Sono davanti
				return true;
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
	}
	
	private static int CwMax = 2000;
	private static int CwMin  = 1000;
	
	@Override
	public void handleAlertMessage(IMessage message){
		new MessageForwarder(message).start();
		
	}
	
}
