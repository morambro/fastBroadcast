package it.unipd.fast.broadcast.location;

//Interface returned on service creation
public interface LocServiceBroadcastInterface {
	void addLocationListener(LocationServiceListener listener);
	void removeLocationListener(LocationServiceListener listener);
}
