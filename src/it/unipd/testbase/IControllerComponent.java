package it.unipd.testbase;

import it.unipd.testbase.eventdispatcher.IComponent;
import it.unipd.testbase.wificonnection.message.IMessage;


public interface IControllerComponent extends IComponent {
	void sendAlert();
	void sendBroadcast(IMessage message);
}
