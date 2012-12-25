package it.unipd.testbase.wificonnection.message;

import java.util.Map;

/**
 * Message interface
 * 
 * @author Fabio De Gaspari
 *
 */
abstract public class IMessage {
	
	
	public static final int DEBUG_MESSAGE = -1;
	

	public static final String CHAR_SEPARATOR = "~!";
	public static final int PING_MESSAGE_TYPE = 0;
	public static final int PARTIAL_FILE_COUNTER_INDEX = 1;
	public static final int ALERT_MESSAGE_TYPE = 2;
	public static final int HELLO_MESSAGE_TYPE = 3;
	public static final int BEGIN_SETUP_FASE = 4;
	public static final int COMPLETE_FILE_COUNTER_INDEX = 5;
	
	
	/**
	 * keys used in getContext of PING_MESSAGE_TYPE messages to address the sender position field  
	 */
	public static final String SENDER_LATITUDE_KEY  = "latitude";
	public static final String SENDER_LONGITUDE_KEY = "longitude";
	/**
	 * Ping message id in messages
	 */
	public static final String PING_MESSAGE_ID_KEY = "send_id_key";
	public static final String FILE_INDEX_KEY = "file_index_key";
	public static final String SENDER_RANGE_KEY = "range";
	public static final String SENDER_DIRECTION_KEY = "direction";
	public static final String MESSAGE_HOP_KEY = "hop_count";
	
	/****************************************************** METHODS *******************************************/
	
	
	public abstract void __debug_set_message(String message);
	public abstract String __debug_get_message();
	
	
	/**
	 * Generates the message, MUST be called after all content is added in order to properly configure the payload
	 */
	public abstract void prepare();
	public abstract void addContent(String contentKey, String content);
	public abstract Map<String, String> getContent();
	public abstract byte[] getMessage();
	public abstract String getMessageString();
	public abstract int getType();
	public abstract int getAppID();
	
	public static String concatContent(String ...contents) {
		String result = contents[0];
		for (int i = 1; i < contents.length; i++) {
			result = result.concat(CHAR_SEPARATOR+contents[i]);
		}
		return result;
	}
	
	public static String[] splitContent(String s)  {
		return s.split(CHAR_SEPARATOR);
	};
}
