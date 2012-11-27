package it.unipd.fast.broadcast.protocol_implementation;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.ITranmissionManager;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
		
		private Random randomGenerator = new Random();
		private IMessage message;
		
		public MessageForwarder(IMessage message) {
			this.message = message;
		}

		@Override
		public void run() {
			
			Map<String,String> content = message.getContent();
			double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
			double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
			int maxRange 		= Integer.valueOf(content.get(IMessage.SENDER_RANGE_KEY));
			int distance = 0; //TODO : calculate distance...
			
			// float[] results = new float[3];
			// Location.distanceBetweenPlaces(latitude, longitude, lon2, lat2)
			
			// calculate contention window
			int contentionWindow = (int)Math.floor((((maxRange-distance)/maxRange) * (CwMax-CwMin))+CwMin); 
			
			// waits for a random time... 
			synchronized (this) {
				try {
					Thread.sleep(randomGenerator.nextInt(contentionWindow));
				} catch (InterruptedException e) {
					e.printStackTrace();
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
	 * Trasmission manager used to send out messages
	 */
	private ITranmissionManager transmissionManager = TransmissionManagerFactory.getInstance().getTransmissionManager();
	
	/**
	 * Tells whether another hello message arrived
	 */
	private Boolean helloMessageArrived = false;
	
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
		helloMessage.addContent(IMessage.SENDER_RANGE_KEY,Math.max(lmfr, cmfr)+"");
		helloMessage.addContent(IMessage.HELLO_SENDER_DIRECTION_KEY,"30");
		
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
				int direction 		= Integer.parseInt(content.get(IMessage.HELLO_SENDER_DIRECTION_KEY));
				
				Location l = new Location(""); // TODO : Retrieve the location
				
				float[] results = new float[3];
				Location.distanceBetween(latitude, longitude, l.getLatitude(), l.getLongitude(), results);
				
				// Received from front
				// if(sameDirection(myDirection,direction))
				cmfr = Math.max(cmfr, Math.max(results[0],max_range));
				// Received from back
				// else
				cmbr = Math.max(cmbr, Math.max(results[0], max_range));

			}
		}.start();
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
