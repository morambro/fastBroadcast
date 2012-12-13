package it.unipd.fast.broadcast;

public interface IComponent {
	public void handle(IEvent event);
	public void register();
}
