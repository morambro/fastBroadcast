package it.unipd.testbase;

import android.os.Handler;

public interface GuiHandlerInterface {
	public static final int SHOW_TOAST_MESSAGE 	= 0;
	public static final int UPDATE_PEERS 		= 1;
	public static final int PROGRESS_MESSAGE 	= 2;
	
	Handler getGuiHandler();
}
