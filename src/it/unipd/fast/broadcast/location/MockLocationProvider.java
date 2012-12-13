package it.unipd.fast.broadcast.location;

import it.unipd.fast.broadcast.EventDispatcher;
import it.unipd.fast.broadcast.event.location.LocationChangedEvent;
import it.unipd.fast.broadcast.helper.LogPrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class MockLocationProvider {
	protected static final String TAG = "it.unipd.fast.broadcast";

	private int __tmp_debug_counter = 1;
	private static void __debug_print_log(String message)
	{
		Log.e(TAG, MockLocationProvider.class.getSimpleName()+": "+message);
	}

	private boolean isSetup = false;
	private int counter;
	private int peersNumber;
	private LocationManager manager;
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

	/**
	 * Positions file
	 */
	private BufferedReader file;

	public MockLocationProvider(LocationManager manager, Context context) {
		Log.d(TAG, this.getClass().getSimpleName()+": registering provider: "+name);
		this.manager = manager;
		// Horrible Workaround: When shutting app from application manager (long press home+swipe) FastBroadcastActivity.onDestroy and, 
		// consequently, doUnbindService get called, but LocationService.onDestroy doesn't for some reason, leaving 
		// mockup-provider registered within the system and causing crash on next startup (Runtime Exception).
		if(manager.getProvider(name)!=null){
			manager.removeTestProvider(name);
		}
		manager.addTestProvider(name, requiresNetwork, requiresSatellite, requiresCell, hasMonetaryCost, 
				supportsAltitude, supportsSpeed, supportsBearing, powerRequirement, accuracy);
		manager.setTestProviderEnabled(name, true);
		try {
			file = new BufferedReader(new InputStreamReader(context.getAssets().open("mock_positions.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void remove() {
		manager.removeTestProvider(name);
	}

	public void updateLocation() {
		try {
			String line = new String("not empty");
			if(firstExec) {
				//skip header line
				line = file.readLine();
				__tmp_debug_counter++;
				line = file.readLine();
				__tmp_debug_counter++;
				bearing = Float.parseFloat(line);
				__debug_print_log("bearing: "+bearing);
				//skip lines according to __counter
				while(line!=null && !line.equals("") && counter!=-1) {
					line = file.readLine();
					if(counter==0)
						__debug_print_log("Current position found in line "+__tmp_debug_counter+": "+line);
					__tmp_debug_counter++;
					counter--;
				}
				firstExec = false;
			}
			else
			{
				int tempFlag = peersNumber-1;
				while((tempFlag != -1) && (line = file.readLine())!=null) {
					if(tempFlag == 0)
					{
						__debug_print_log("Current position found in line "+__tmp_debug_counter+": "+line);
						__tmp_debug_counter++;
					}
					else
					{
						__tmp_debug_counter++;
					}
					tempFlag--;
				}
			}
			if(line == null) {//TODO: end of file reached, shutdown the simulation
				Log.d(TAG, this.getClass().getSimpleName()+": end of file reached");
				return;
			}
			String[] positions = line.split(",");
			Location location = new Location(name);
			location.setLatitude(Double.valueOf(positions[0]));
			location.setLongitude(Double.valueOf(positions[1]));
			location.setTime(System.currentTimeMillis());
			location.setBearing(bearing);
			Log.d(TAG, this.getClass().getSimpleName()+": NEW LOCATION: "+location.getBearing()+"; "+location.getLatitude()+"; "+location.getLongitude());
			LogPrinter.getInstance().writeTimedLine("current position in car queue is "+(__tmp_debug_counter-2));
			manager.setTestProviderLocation(name, location);
			EventDispatcher.getInstance().triggerEvent(new LocationChangedEvent(location));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setup(int counter, int peersNumber) {
		isSetup = true;
		Log.d(TAG, MockLocationProvider.class.getSimpleName()+": file counter: "+counter+"; peers number: "+peersNumber);
		__debug_print_log("file counter: "+counter+"; peers number: "+peersNumber);
		this.counter = counter;
		this.peersNumber = peersNumber;
	}
	
	public boolean isSetup() {
		return isSetup;
	}
}