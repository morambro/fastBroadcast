package it.unipd.fast.broadcast.location;

import it.unipd.fast.broadcast.event.IEvent;
import it.unipd.fast.broadcast.event.location.SetupProviderEvent;
import it.unipd.fast.broadcast.event.location.UpdateLocationEvent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class MockLocationService extends LocationService implements IMockLocationComponent {

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

	@Override
	public void updateLocation() {
		mockProvider.updateLocation();
	}

	@Override
	public void setup(int counter, int peersNumber) {
		if(!mockProvider.isSetup())
			mockProvider.setup(counter, peersNumber);
		else
			Log.e(TAG, this.getClass().getSimpleName()+": double setup attempt!");
	}
	
	@Override
	public void handle(IEvent event) {
		Log.d(TAG,this.getClass().getSimpleName()+" : Handle called +"+event.getClass().getSimpleName());
		if(event instanceof UpdateLocationEvent) {
			updateLocation();
			return;
		}
		if(event instanceof SetupProviderEvent) {
			SetupProviderEvent ev = (SetupProviderEvent) event;
			setup(ev.counter, ev.peersNumber);
		}
	}
}
