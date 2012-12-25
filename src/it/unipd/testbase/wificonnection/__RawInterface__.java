package it.unipd.testbase.wificonnection;

import it.unipd.testbase.helper.Log;

public class __RawInterface__ {
	protected static final String TAG = "it.unipd.testbase";
	public static final int TRANSMISSION_ERROR = -1;
	private static int clinetCounter = 0;
	private static Long rawSocket = null;
	
	public static long get_raw_socket() {
		if(rawSocket == null)
			rawSocket = m_get_raw_socket();
		clinetCounter++;
		return rawSocket;
	}
	
	public static void clear_raw_socket() {
		--clinetCounter;
		Log.e(TAG, __RawInterface__.class.getSimpleName()+": clinetCounter "+clinetCounter);
		if(clinetCounter==0) {
			m_clear_raw_socket(rawSocket);
		}
	}
	
	static public native int native_send(long sd_ptr, String data, String[] error);
	static protected native long m_get_raw_socket();
	static public native String native_read(long sd_ptr, int[] read);
	static protected native void m_clear_raw_socket(long sd_ptr);
}
