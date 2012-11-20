package it.unipd.fast.broadcast.location;

import android.location.LocationListener;
import android.location.LocationManager;

public class MockLocationService extends LocationService {
	
	private MockLocationProvider mockProvider;
	
	@Override
	protected void registerLocationProviders(LocationManager locationManager, LocationListener listener) {
		mockProvider = new MockLocationProvider(locationManager, this);
		locationManager.requestLocationUpdates(mockProvider.name, 0, 0, listener);
	}
	
	@Override
	protected void cleanup() {
		mockProvider.remove();
	}
}
