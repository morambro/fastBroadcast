package it.unipd.testbase;

import android.os.Handler;

public interface GuiHandlerInterface {
	public static final int SHOW_TOAST_MSG = 0;
	public static final int UPDATE_PEERS = 1;

	Handler getGuiHandler();
}
