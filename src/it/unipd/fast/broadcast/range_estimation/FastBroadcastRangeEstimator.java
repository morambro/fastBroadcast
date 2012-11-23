package it.unipd.fast.broadcast.range_estimation;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverService;
import it.unipd.fast.broadcast.wifi_connection.receiver.DataReceiverServiceInterface;
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

/**
 * An implementation of fast broadcast estimation phase.
 * 
 * @author Moreno Ambrosin
 *
 */
public class FastBroadcastRangeEstimator extends Service implements IRangeEstimator{
	
	/******************************************* DECLARATIONS ******************************************/
	
	/**
	 * Task which is scheduled at a fixed time. 
	 */
	private class HelloMessageSender extends TimerTask{
		
		private Random randomGenerator = new Random();
		
		@Override
		public void run(){
			synchronized (this) {
				helloMessageArrived = false;
			}
			randomGenerator.setSeed(TURN_DURATION);
			int randomTime = randomGenerator.nextInt();
			try{
				wait(randomTime);
			}catch(InterruptedException ex){
				stopExecuting();
				ex.printStackTrace();
			}
			// After waiting a random time check whether another hello message arrived,
			// and if not, sends an hello message
			if(!helloMessageArrived){
				sendHelloMessage();
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
	
	private int lmfr = 300;
	
	private int lmbr = 300;
	
	private Timer scheduler;
	
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
		// TODO : Hello Message receiver
	}
	
	@Override
	public void stopExecuting(){
		if(scheduler != null){
			scheduler.cancel();
		}
	}
	
	@Override
	public void setDevicesList(List<String> devices) {
		this.devices = devices;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new RangeEstimatorBinder();
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		// On service creation, starts hello message sender Scheduler
		// creating a timer to schedule hello message sending
		scheduler = new Timer();
		// Start scheduling
		scheduler.schedule(new HelloMessageSender(),TURN_DURATION,TURN_DURATION);
	}
}
