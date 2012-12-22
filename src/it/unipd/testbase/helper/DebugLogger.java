package it.unipd.testbase.helper;

import android.util.Log;

public class DebugLogger {
	
	public static final String TAG = "it.unipd.testbase";
	
	private Class<?> classToDebug;
	
	public DebugLogger(Class<?> classToDebug) {
		this.classToDebug = classToDebug;
	}
	
	public void d(String message){
		Log.d(TAG,classToDebug.getSimpleName()+" : "+message);
	}
	
	public void e(Exception e){
		Log.e(TAG,"ECCEZIONE",e);
	}
}

