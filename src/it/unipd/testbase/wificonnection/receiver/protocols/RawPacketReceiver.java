package it.unipd.testbase.wificonnection.receiver.protocols;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.message.MessageReceivedEvent;
import it.unipd.testbase.wificonnection.__RawInterface__;
import it.unipd.testbase.wificonnection.message.IMessage;
import it.unipd.testbase.wificonnection.message.MessageBuilder;
import it.unipd.testbase.wificonnection.receiver.AbstractPacketReceiver;

public class RawPacketReceiver extends AbstractPacketReceiver {
	protected final String TAG = "it.unipd.testbase";

	private boolean isSocketClear = false;
	protected final int PACKET_SIZE = 600;

	protected long socket=0;

	public RawPacketReceiver() {
		socket = __RawInterface__.get_raw_socket();
	}

	@Override
	public void run() {
		while(!terminated){
			final int readB[] = new int[1];
			final String sender[] = new String[1];
			final String message = __RawInterface__.native_read(socket, readB);
			//Log.d(TAG,RawPacketReceiver.class.getSimpleName() +" read "+readB[0]+" Bytes");
			if(readB[0] != __RawInterface__.TRANSMISSION_ERROR) {
				new Thread(){
					public void run(){

						IMessage xmlMessage = MessageBuilder.getInstance().getMessage(message);

						//Log.d(TAG,RawPacketReceiver.class.getSimpleName() +"message : " + message);

						EventDispatcher.getInstance().triggerEvent(new MessageReceivedEvent(xmlMessage , sender[0]));
					}
				}.start();
			}
		}
		if(!isSocketClear) {
			__RawInterface__.clear_raw_socket();
			isSocketClear = true;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(!isSocketClear) {
			__RawInterface__.clear_raw_socket();
			isSocketClear = true;
		}
		super.finalize();
	}

	@Override
	public void terminate() {
		this.terminated = false;
	}


}
