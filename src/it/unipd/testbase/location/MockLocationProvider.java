package it.unipd.testbase.location;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.location.PositionsTerminatedEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class MockLocationProvider {
	protected static final String TAG = "it.unipd.testbase";

//	private int __tmp_debug_counter = 1;
	
	private static void __debug_print_log(String message){
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
	
	private List<String> lines;
	int currentIndex = 1;

	/**
	 * Positions file
	 */
	private BufferedReader file;

	public MockLocationProvider(LocationManager manager, Context context) {
		Log.d(TAG, this.getClass().getSimpleName()+": registering provider: "+name);
		this.manager = manager;
		// Horrible Workaround: When shutting app from application manager (long press home+swipe) TestBaseActivity.onDestroy and, 
		// consequently, doUnbindService get called, but LocationService.onDestroy doesn't for some reason, leaving 
		// mockup-provider registered within the system and causing crash on next startup (Runtime Exception).
		if(manager.getProvider(name)!=null) {
			manager.removeTestProvider(name);
		}
		manager.addTestProvider(name, requiresNetwork, requiresSatellite, requiresCell, hasMonetaryCost, 
				supportsAltitude, supportsSpeed, supportsBearing, powerRequirement, accuracy);
		manager.setTestProviderEnabled(name, true);
		
		
		try {
			file = new BufferedReader(new InputStreamReader(context.getAssets().open("mock_positions.txt")));
			lines = new ArrayList<String>();
			String strLine;
			//Read File Line By Line
			while ((strLine = file.readLine()) != null)   {
			// Print the content on the console
				lines.add(strLine);
			}
			
			bearing = Float.parseFloat(lines.get(currentIndex));
			currentIndex++;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void remove() {
		manager.removeTestProvider(name);
	}

	public void updateLocation() {
//		try {
//			String line = new String("not empty");
//			if(firstExec) {
//				//skip header line
//				line = file.readLine();
//				__tmp_debug_counter++;
//				line = file.readLine();
//				__tmp_debug_counter++;
//				bearing = Float.parseFloat(line);
//				__debug_print_log("bearing: "+bearing);
//				//skip lines according to __counter
//				while(line!=null && !line.equals("") && counter!=-1) {
//					line = file.readLine();
//					if(counter==0){
//						__debug_print_log("Current position found in line "+__tmp_debug_counter+": "+line);
//					}
//					__tmp_debug_counter++;
//					counter--;
//				}
//				firstExec = false;
//			} else {
//				int tempFlag = peersNumber-1;
//				while((tempFlag != -1) && (line = file.readLine())!=null) {
//					if(tempFlag == 0)
//					{
//						__debug_print_log("Current position found in line "+__tmp_debug_counter+": "+line);
//						__tmp_debug_counter++;
//					}
//					else
//					{
//						__tmp_debug_counter++;
//					}
//					tempFlag--;
//				}
//			}
			if(firstExec){
				currentIndex = currentIndex + counter;
				firstExec = false;
			} else {
				currentIndex = currentIndex + peersNumber;
			}
			
			if(lines.size() <= currentIndex){
				// TODO : PROBLEMA
				Log.d(TAG, this.getClass().getSimpleName()+": end of file reached");
				EventDispatcher.getInstance().triggerEvent(new PositionsTerminatedEvent());
				return;
			}
			__debug_print_log("Current position found in line "+(currentIndex+1)+": "+lines.get(currentIndex));
//			if(line == null) {//TODO: end of file reached, shutdown the simulation
//				Log.d(TAG, this.getClass().getSimpleName()+": end of file reached");
//				return;
//			}
//			String[] positions = line.split(",");
			String[] positions = lines.get(currentIndex).split(",");
			Location location = new Location(name);
			location.setLatitude(Double.valueOf(positions[0]));
			location.setLongitude(Double.valueOf(positions[1]));
			location.setTime(System.currentTimeMillis());
			location.setBearing(bearing);
			__debug_print_log(": NEW LOCATION: "+location.getBearing()+"; "+location.getLatitude()+"; "+location.getLongitude());
//			LogPrinter.getInstance().writeTimedLine("current position in car queue is "+(__tmp_debug_counter-2));
			__debug_print_log("Current position found in car queue is "+(currentIndex+1));
			manager.setTestProviderLocation(name, location);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void setup(int counter, int peersNumber) {
		isSetup = true;
		Log.d(TAG, MockLocationProvider.class.getSimpleName()+": file counter: "+counter+"; peers number: "+peersNumber);
		this.counter = counter;
		this.peersNumber = peersNumber;
	}
	
	public boolean isSetup() {
		return isSetup;
	}
}