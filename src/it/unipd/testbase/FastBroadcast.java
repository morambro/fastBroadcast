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
}
