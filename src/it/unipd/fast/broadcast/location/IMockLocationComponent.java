package it.unipd.fast.broadcast.location;

public interface IMockLocationComponent extends ILocationComponent {
	public void updateLocation();
	public void setup(int counter, int peersNumber);
}
