package it.unipd.testbase.location;

import it.unipd.testbase.eventdispatcher.EventDispatcher;
import it.unipd.testbase.eventdispatcher.event.IEvent;
import it.unipd.testbase.eventdispatcher.event.location.LocationChangedEvent;
import it.unipd.testbase.eventdispatcher.event.location.SetupProviderEvent;
import it.unipd.testbase.eventdispatcher.event.location.UpdateLocationEvent;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class LocationService extends Service implements ILocationComponent {
	protected final String TAG = "it.unipd.testbase";

	protected static final long TIME_THRESHOLD = 60000;//ms => 1 min
	protected static final int ACCURACY_THRESHOLD = 50;//mt
	protected List<LocationServiceListener> listeners = new ArrayList<LocationServiceListener>();
	protected Location lastLoc = null;
	protected LocationManager locationManager;
	protected LocationListener listener;

	//Service binder definition
	public class LocServiceBinder extends Binder {
		public ILocationComponent getService() {
			return LocationService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new LocServiceBinder();
	}

	//LocServiceBroadcast implementation
	public void addLocationListener(LocationServiceListener listener) {
		listeners.add(listener);
	}

	public void removeLocationListener(LocationServiceListener listener) {
		listeners.remove(listener);
	}
	
//	@Override
//	public MockLocationProvider __get_mock_provider() {
//		return null;
//	}

	//Service methods
	@Override
	public void onCreate() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		listener = new LocationListener() {

			public void onStatusChanged(String provider, int status, Bundle extras) {
				switch(status) {
				case LocationProvider.OUT_OF_SERVICE:
					Log.d(TAG, this.getClass().getSimpleName()+": provider "+provider+" out of service");
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					Log.d(TAG, this.getClass().getSimpleName()+": provider "+provider+" temporarily unavailable");
					break;
				case LocationProvider.AVAILABLE:
					Log.d(TAG, this.getClass().getSimpleName()+": provider "+provider+" available again");
					break;
				}
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}

			public void onLocationChanged(Location location) {
				if(useNewLocation(location, lastLoc)) {
//					for (LocationServiceListener locListener : listeners) {
//						locListener.onLocationChanged(location);
//						lastLoc = location;
//					}
					EventDispatcher.getInstance().triggerEvent(new LocationChangedEvent(location));
				}
			}
		};
		registerLocationProviders(locationManager, listener);
		register();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, this.getClass().getSimpleName()+": Unregistering location listener..");
		cleanup();
		locationManager.removeUpdates(listener);
		super.onDestroy();
	}

	protected void registerLocationProviders(LocationManager locationManager, LocationListener listener) {
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
	}

	protected void cleanup() {

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

	@Override
	public void handle(IEvent event) {
	}
	
	@Override
	public void register() {
		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
		events.add(UpdateLocationEvent.class);
		events.add(SetupProviderEvent.class);
		EventDispatcher.getInstance().registerComponent(this, events);
		Log.d(TAG,this.getClass().getSimpleName()+" : service regitered to EventDispatcher");
	}

}
