package it.unipd.fast.broadcast.location;

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
	private void __debug_print_log(String message)
	{
		Log.e(TAG, this.getClass().getSimpleName()+": "+message);
	}


	private static int __counter;
	private static int __peers_number;


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
			String line = "not null";
			if(firstExec) {
				//skip header line
				line = file.readLine();
				__debug_print_log("line "+__tmp_debug_counter+": "+line+" discarded");
				__tmp_debug_counter++;
				line = file.readLine();
				__debug_print_log("line "+__tmp_debug_counter+": "+line+" discarded");
				__tmp_debug_counter++;
				bearing = Float.parseFloat(line);
				//skip lines according to __counter
				while(line!=null && __counter!=0) {
					line = file.readLine();
					Log.d(TAG, this.getClass().getSimpleName()+": discarding line "+line);
					__debug_print_log("Current position found in line "+__tmp_debug_counter+": "+line);
					__tmp_debug_counter++;
					__counter--;
				}
				firstExec = false;
			}
			int tempFlag = __peers_number-1;
			while(( line != null || !line.equals(""))&& tempFlag != -1) {
				if(tempFlag == 0)
				{
					line = file.readLine();
					__debug_print_log("Current position found in line "+__tmp_debug_counter+": "+line);
					__tmp_debug_counter++;
				}
				else
				{
					String discarded = file.readLine();
					__debug_print_log("line "+__tmp_debug_counter+": "+discarded+" discarded");
					__tmp_debug_counter++;
				}
				tempFlag--;
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
			manager.setTestProviderLocation(name, location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void __set_static_couter(int __counter, int __peers_number) {
		Log.d(TAG, MockLocationProvider.class.getSimpleName()+": file counter: "+__counter+"; peers number: "+__peers_number);
		MockLocationProvider.__counter = __counter;
		MockLocationProvider.__peers_number = __peers_number;
	}
}