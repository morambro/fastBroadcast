package it.unipd.fast.broadcast;

import android.content.Context;


public class EventDispatcher {
	
	private static EventDispatcher singleton = null;
	private Context context;
	
	public static EventDispatcher getInstance(Context context) {
		if(singleton==null)
			singleton = new EventDispatcher(context);
		return singleton;
	}
	
	protected EventDispatcher(Context context)
	{
		this.context = context;
	}
	
	public void registerListener() {
		
	}
}
