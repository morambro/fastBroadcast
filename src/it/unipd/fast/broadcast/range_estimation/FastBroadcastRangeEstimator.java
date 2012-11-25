package it.unipd.fast.broadcast.range_estimation;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.ITranmissionManager;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * An implementation of fast broadcast estimation phase.
 * 
 * @author Moreno Ambrosin
 *
 */
public class FastBroadcastRangeEstimator extends Service implements IRangeEstimator{
	
	private String TAG = "it.unipd.fast.broadcast";
	
	/******************************************* DECLARATIONS ******************************************/
	
	/**
	 * Task scheduled at a fixed TURN_DURATION time, which sends out an hello message 
	 * to perform Range estimation
	 */
	private class HelloMessageSender extends TimerTask{
		
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
	 * Binder to get service interface
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	public class RangeEstimatorBinder extends Binder {
		
		public IRangeEstimator getService() {
			return FastBroadcastRangeEstimator.this;
		}
	}
	
	/**
	 * list of all the devices in the network
	 */
	private List<String> devices;
	/**
	 * Current-turn Maximum Front Range
	 */
	private int cmfr = 300;
	/**
	 * Current-turn Maximum Back Range
	 */
	private int cmbr = 300;
	/**
	 * Last-turn Maximum Front Range
	 */
	private int lmfr = 300;
	/**
	 * Last-turn Maximum Back Range
	 */
	private int lmbr = 300;
	
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
		// TODO : use location service to set a position
		helloMessage.addContent("my_position","position");
		// Adding max range 
		helloMessage.addContent("max_range",Math.max(lmfr, cmfr)+"");
		helloMessage.prepare();
		transmissionManager.send(devices, helloMessage);
	}
	
	@Override
	public synchronized void setHelloMessageArrived(boolean arrived){
		this.helloMessageArrived = arrived;
	}
	
	@Override
	public void helloMessageReceived(IMessage message){
	}
	
	@Override
	public void stopExecuting(){
		if(scheduler != null){
			scheduler.cancel();
			scheduler.purge();
		}
	}
	
	@Override
	public void setDevicesList(List<String> devices) {
		this.devices = devices;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		this.devices = intent.getStringArrayListExtra("devices");
		// Start scheduling on service binding
		scheduler.schedule(new HelloMessageSender(),TURN_DURATION,TURN_DURATION);
		return new RangeEstimatorBinder();
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
}
