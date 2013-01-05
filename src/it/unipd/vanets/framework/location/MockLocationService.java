package it.unipd.vanets.framework.location;

import it.unipd.vanets.framework.eventdispatcher.event.IEvent;
import it.unipd.vanets.framework.eventdispatcher.event.location.SetupProviderEvent;
import it.unipd.vanets.framework.eventdispatcher.event.location.UpdateLocationEvent;
import it.unipd.vanets.framework.helper.DebugLogger;
import android.location.LocationListener;
import android.location.LocationManager;

public class MockLocationService extends LocationService implements IMockLocationComponent {

	private MockLocationProvider mockProvider;
	private DebugLogger logger = new DebugLogger(MockLocationService.class);
	
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
			logger.d("Double setup attempt!");
	}
	
	@Override
	public void handle(IEvent event) {
		logger.d("Handle called "+event.getClass().getSimpleName());
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
