package it.unipd.fast.broadcast;

import it.unipd.fast.broadcast.event.IEvent;

public interface IComponent {
	public void handle(IEvent event);
	public void register();
}
