package it.unipd.testbase.wificonnection.transmissionmanager;

import it.unipd.testbase.wificonnection.__RawInterface__;
import it.unipd.testbase.wificonnection.message.IMessage;

public class RawTransmissionManager implements ITranmissionManager{
	private static final String TAG = "it.unipd.testbase";
	
	private long sd = 0;
	
	public RawTransmissionManager() {
		sd = __RawInterface__.get_raw_socket();
	}
	
	@Override
	public void send(final String ID,final IMessage msg) {

		new Thread(){
			public void run(){
				int messageLength = msg.getMessage().length;
				String error[] = new String[1];
				__RawInterface__.native_send(sd, msg.getMessageString(), error);
			}
		}.start();	}

	@Override
	public void release() {
		__RawInterface__.clear_raw_socket();
	}
}
