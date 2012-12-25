package it.unipd.testbase.eventdispatcher.event.gui;

import it.unipd.testbase.eventdispatcher.event.IEvent;

public class UpdateGuiEvent implements IEvent {
	public static final int GUI_UPDATE_NEW_MESSAGE = 1;
	public static final int GUI_UPDATE_CONT_WINDOW_START = 2;
	public static final int GUI_UPDATE_MESSAGE_FORWARDED = 3;
	public static final int GUI_UPDATE_MESSAGE_NOT_FORWARDED = 4;
	public static final int GUI_UPDATE_ADD_PEER = 5;
	public static final int GUI_UPDATE_UNLOCK = 6;
	
	public int type;
	public Object obj;

	public UpdateGuiEvent(int type, Object obj)
	{
		this.type = type;
		this.obj = obj;
	}
}
