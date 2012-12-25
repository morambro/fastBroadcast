package it.unipd.testbase.wificonnection.receiver;

import it.unipd.testbase.AppController;
import it.unipd.testbase.AppController.IDataCollectionHandler;
import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.gui.UpdateGuiEvent;
import it.unipd.testbase.eventdispatcher.event.message.BeginSetupEvent;
import it.unipd.testbase.eventdispatcher.event.message.CompleteFileIndexReceivedEvent;
import it.unipd.testbase.eventdispatcher.event.message.PartialFileIndexReceivedEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.AlertMessageArrivedEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.HelloMessageArrivedEvent;
import it.unipd.testbase.helper.Log;
import it.unipd.testbase.helper.LogPrinter;
import it.unipd.testbase.wificonnection.message.IMessage;

public class CollectionHandler implements IDataCollectionHandler {
	private static final String TAG = "it.unipd.testbase";

	@Override
	public void onDataCollected(IMessage message, String hostIp) {

		int messageType = message.getType();
		int appID = message.getAppID();

		if(appID == AppController.getApplicatonRunID()) {
			Log.e(TAG, this.getClass().getSimpleName()+": Message of type "+messageType+" from same source "+appID+", discarded");
			return;
		}

		switch(messageType){
		case IMessage.PING_MESSAGE_TYPE :
			//TODO: map devices to position file
			break;

		case IMessage.ALERT_MESSAGE_TYPE :
			LogPrinter.getInstance().writeTimedLine("alert message received from "+hostIp+". Hop number: "+message.getContent().get(IMessage.MESSAGE_HOP_KEY));
			Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto ALERT : \n"+message);
			EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_NEW_MESSAGE, processMessageForEvent(message)));
			EventDispatcher.getInstance().triggerEvent(new AlertMessageArrivedEvent(message));
			break;

			// case of fast broadcast hello message
		case IMessage.HELLO_MESSAGE_TYPE :
			Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  HELLO: \n");
			EventDispatcher.getInstance().triggerEvent(new HelloMessageArrivedEvent(message));
			break;

		case IMessage.BEGIN_SETUP_FASE:
			Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  BEGIN_SETUP: \n"+message);
			//int id = Integer.valueOf(message.getContent().get(IMessage.FILE_INDEX_KEY));
			//if(id!=AppController.getApplicatonRunID())
			EventDispatcher.getInstance().triggerEvent(new BeginSetupEvent());
			//else
			//	Log.d(TAG, this.getClass().getSimpleName()+": Same app id, message discarded");
			break;

		case IMessage.PARTIAL_FILE_COUNTER_INDEX:
			Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  PARTIAL_FILE_COUNTER_INDEX: \n"+message);
			PartialFileIndexReceivedEvent ev = new PartialFileIndexReceivedEvent(Integer.valueOf(message.getAppID()));
			EventDispatcher.getInstance().triggerEvent(ev);
			break;

		case IMessage.COMPLETE_FILE_COUNTER_INDEX:
			Log.d(TAG, this.getClass().getSimpleName()+": Ricevuto  COMPLETE_FILE_COUNTER_INDEX: \n"+message);
			CompleteFileIndexReceivedEvent ev1 = new CompleteFileIndexReceivedEvent(message.getContent());
			EventDispatcher.getInstance().triggerEvent(ev1);
			break;

			// Ignoring unknown messages
		default : 
			Log.d(TAG, this.getClass().getSimpleName()+": Unknown message type "+messageType+", discarded.");
		}
	}

	private String processMessageForEvent(IMessage message)
	{
		String result = "ALERT - time: "+System.currentTimeMillis()+", hops: "+message.getContent().get(IMessage.MESSAGE_HOP_KEY);
		return result;
	}
}
