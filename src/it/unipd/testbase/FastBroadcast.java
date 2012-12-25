package it.unipd.testbase;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.ShutdownEvent;
import it.unipd.testbase.gui.Gui;
import it.unipd.testbase.helper.Log;
import it.unipd.testbase.location.MockLocationService;
import it.unipd.testbase.protocol.FastBroadcastService;
import it.unipd.testbase.wificonnection.receiver.DataReceiverService;
import it.unipd.testbase.wificonnection.transmissionmanager.TransmissionManagerFactory;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FastBroadcast {
	protected static final String TAG = "it.unipd.testbase";

	private Gui gui;
	private AppController controller;

	static class ShutdownHook extends Thread {
		@Override
		public void run() {
			Log.d(TAG, "ShutdownHook: Hook called");
			EventDispatcher.getInstance().triggerEvent(new ShutdownEvent());
		}
	}

	public static void main(String[] args) {
		System.loadLibrary("jni_rawsocket");
		if(!isRoot())
		{
			System.out.println("Application must run as root");
			System.exit(-1);
		}
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		FastBroadcast fb = new FastBroadcast();
	}

	public FastBroadcast() {
		setupGui();
		FastBroadcastService.getInstance();
		DataReceiverService.getInstance();
		TransmissionManagerFactory.getInstance();
		MockLocationService.getInstance();
		controller = new AppController();
	}

	private void setupGui() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui = new Gui();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static boolean isRoot() {
		try{
			String userName = System.getProperty("user.name");
			String command = "id -u "+userName;
			Process child = Runtime.getRuntime().exec(command);
			// Get the input stream and read from it
			BufferedReader in = new BufferedReader(new InputStreamReader(child.getInputStream()));
			int c = Integer.valueOf(in.readLine());
			in.close();
			return c==0 ? true : false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	//	protected static void cleanup() {
	//	FastBroadcastService.getInstance().terminate();
	//	DataReceiverService.getInstance().terminate();
	//	MockLocationService.getInstance().terminate();
	//	Log.d(TAG, FastBroadcast.class.getSimpleName()+": Cleanup called");
	//
	//}

	//	@Override
	//	public void register() {
	//		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
	//		events.add(MessageReceivedEvent.class);
	//		events.add(SendBroadcastMessageEvent.class);
	//		EventDispatcher.getInstance().registerComponent(this, events);
	//	}
	//
	//	@Override
	//	public void handle(IEvent event) {
	//		if(event instanceof MessageReceivedEvent){
	//			MessageReceivedEvent ev = (MessageReceivedEvent) event;
	//			String message = ev.message.__debug_get_message();
	//			EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_NEW_MESSAGE, message));
	//			EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_CONT_WINDOW_START, new Integer[]{500, 1024}));
	//			return;
	//		}
	//		if(event instanceof SendBroadcastMessageEvent){
	//			SendBroadcastMessageEvent ev = (SendBroadcastMessageEvent) event;
	//			IMessage temp = MessageBuilder.getInstance().getMessage("");
	//			temp.__debug_set_message("message sent");
	//			Log.d(TAG, this.getClass().getSimpleName()+": sending message");
	//			TransmissionManagerFactory.getInstance().getTransmissionManager().send("", temp);
	//			return;
	//		}
	//	}
}
