package it.unipd.testbase.location;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.ShutdownEvent;
import it.unipd.testbase.eventdispatcher.event.gui.UpdateGuiEvent;
import it.unipd.testbase.eventdispatcher.event.location.LocationChangedEvent;
import it.unipd.testbase.eventdispatcher.event.location.SetupProviderEvent;
import it.unipd.testbase.eventdispatcher.event.location.UpdateLocationEvent;
import it.unipd.testbase.eventdispatcher.event.protocol.StopSimulationEvent;
import it.unipd.testbase.helper.Log;
import it.unipd.testbase.helper.LogPrinter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockLocationService implements IMockLocationComponent{
	protected static final String TAG = "it.unipd.testbase";
	private static MockLocationService instance = null;


	protected static final long TIME_THRESHOLD = 60000;//ms
	protected static final int ACCURACY_THRESHOLD = 50;//m
	protected Location lastLoc = null;
	private boolean isSetup = false;
	private int counter;
	private int peersNumber;
	private boolean firstExec = true;
	public String name = "MockProvider";
	public boolean requiresNetwork = false;
	public boolean requiresSatellite = false;
	public boolean requiresCell = false;
	public boolean hasMonetaryCost = false;
	public boolean supportsAltitude = false;
	public boolean supportsSpeed = false;
	public boolean supportsBearing = true;
	public int powerRequirement = 0;
	public int accuracy = 2;
	private float bearing;
	private BufferedReader file;

	private List<String> lines;
	int currentIndex = 1;


	/*				DEBUG & PROFILING STUFF				*/
	private int __tmp_debug_counter = 1;
	private static void __debug_print_log(String message)
	{
		Log.e(TAG, MockLocationService.class.getSimpleName()+": "+message);
	}

	public static IMockLocationComponent getInstance()
	{
		if(instance==null)
			instance = new MockLocationService();
		return instance;
	}

	@Override
	public void terminate()
	{
		Log.d(TAG,this.getClass().getSimpleName()+": service terminated");
		instance = null;
	}

	@Override
	public void handle(IEvent event) {
		if(event instanceof UpdateLocationEvent) {
			updateLocation();
			return;
		}
		if(event instanceof SetupProviderEvent) {
			SetupProviderEvent ev = (SetupProviderEvent) event;
			setup(ev.counter, ev.peersNumber);
		}
		if(event instanceof ShutdownEvent) {
			terminate();
			return;
		}
	}

	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(UpdateLocationEvent.class);
		events.add(SetupProviderEvent.class);
		events.add(ShutdownEvent.class);
		EventDispatcher.getInstance().registerComponent(this, events);
		Log.d(TAG,this.getClass().getSimpleName()+" : service regitered to EventDispatcher");
	}

	protected MockLocationService() {
		try {
			file = new BufferedReader(new FileReader("../assets/mock_positions.txt"));
			lines = new ArrayList<String>();
			String strLine;
			while ((strLine = file.readLine()) != null)   {
				lines.add(strLine);
			}
			bearing = Float.parseFloat(lines.get(currentIndex));
			currentIndex++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		register();
	}

	private void updateLocation() {
		if(firstExec){
			currentIndex = currentIndex + counter;
			firstExec = false;
		} else {
			currentIndex = currentIndex + peersNumber;
		}
		
		if(lines.size() <= currentIndex){
			Log.d(TAG,this.getClass().getSimpleName()+": End of file reached");
			// Trigger an event to communicate the end of positions
			EventDispatcher.getInstance().triggerEvent(new UpdateGuiEvent(UpdateGuiEvent.GUI_UPDATE_NEW_MESSAGE, "SIMULATION TERMINATED"));
			EventDispatcher.getInstance().triggerEvent(new StopSimulationEvent());
			return;
		}
		LogPrinter.getInstance().writeLine("Current position in file = "+(currentIndex+1));
		Log.d(TAG,this.getClass().getSimpleName()+": Current position found in line "+(currentIndex+1)+": "+lines.get(currentIndex));
		String[] positions = lines.get(currentIndex).split(",");
		Location location = new Location(name);
		location.setLatitude(Double.valueOf(positions[0]));
		location.setLongitude(Double.valueOf(positions[1]));
		location.setTime(System.currentTimeMillis());
		location.setBearing(bearing);
		//Log.d(TAG,this.getClass().getSimpleName()+": NEW LOCATION: "+location.getBearing()+"; "+location.getLatitude()+"; "+location.getLongitude());
		//Log.d(TAG,this.getClass().getSimpleName()+": Current position found in car queue is "+(currentIndex+1));
		EventDispatcher.getInstance().triggerEvent(new LocationChangedEvent(location));
	}

	private void setup(int counter, int peersNumber) {
		if(isSetup)
		{
			Log.e(TAG, this.getClass().getSimpleName()+": double setup attempt");
			return;
		}
		isSetup = true;
		Log.d(TAG, MockLocationService.class.getSimpleName()+": file counter: "+counter+"; peers number: "+peersNumber);
		this.counter = counter;
		this.peersNumber = peersNumber;
		updateLocation();
	}


	protected boolean useNewLocation(Location newLoc, Location oldLoc) {
		if(oldLoc==null)
			return true;
		long timeDiff = newLoc.getTime() - oldLoc.getTime();
		int accDiff = (int) ( newLoc.getAccuracy() - oldLoc.getAccuracy());
		boolean isNewer = timeDiff>0;
		boolean lessAcc = accDiff>0;
		if(timeDiff > TIME_THRESHOLD)
			return true;
		if(timeDiff < -TIME_THRESHOLD)
			return false;
		if(accDiff > ACCURACY_THRESHOLD)
			return false;
		if(accDiff<0)
			return true;
		if(lessAcc && isNewer)
			return true;
		return false;
	}
}