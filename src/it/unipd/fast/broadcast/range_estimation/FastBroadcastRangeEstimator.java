package it.unipd.fast.broadcast.range_estimation;

import it.unipd.fast.broadcast.wifi_connection.message.IMessage;
import it.unipd.fast.broadcast.wifi_connection.message.MessageBuilder;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.ITranmissionManager;
import it.unipd.fast.broadcast.wifi_connection.transmissionmanager.TransmissionManagerFactory;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An implementation of fast broadcast estimation phase.
 * 
 * @author Moreno Ambrosin
 *
 */
public class FastBroadcastRangeEstimator implements IRangeEstimator{
	
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
	 * Class used to handle the received Hello message
	 * 
	 * @author Moreno Ambrosin
	 *
	 */
	private class ReceivedHelloMessagesHandler extends Thread{
		
		private IMessage messageToHandle;
		
		public ReceivedHelloMessagesHandler(IMessage helloMessage) {
			messageToHandle = helloMessage;
		}
		
		public void run(){
			// TODO: handle hello message, update integer variables
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
	
	public FastBroadcastRangeEstimator(List<String> devices) {
		this.devices = devices;
	}
	
	@Override
	public void initHelloSender() {
		// creating a timer to schedule hello message sending
		scheduler = new Timer();
		// Start scheduling
		scheduler.schedule(new HelloMessageSender(),TURN_DURATION,TURN_DURATION);
	}
	
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
		// creates a new handle to extract information from the message
		new ReceivedHelloMessagesHandler(message).start();
	}
	
	public void stopExecuting(){
		if(scheduler != null){
			scheduler.cancel();
		}
	}

}
