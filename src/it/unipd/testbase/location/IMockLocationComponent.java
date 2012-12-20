package it.unipd.testbase.location;

public interface IMockLocationComponent extends ILocationComponent {
	public void updateLocation();
	public void setup(int counter, int peersNumber);
}
