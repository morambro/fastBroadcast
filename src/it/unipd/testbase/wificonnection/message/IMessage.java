package it.unipd.testbase.wificonnection.message;

import java.util.Map;

/**
 * Message interface
 * 
 * @author Fabio De Gaspari
 *
 */
abstract public class IMessage {
	/**
	 * Address used to identify broadcast messages
	 */
	public static final String CHAR_SEPARATOR = "~!";
	public static final int FILE_COUNTER_INDEX = 1;
	
	/**
	 * Ping message identification
	 */
	public static final int PING_MESSAGE_TYPE = 0;
	
	/**
	 * Ping message id in messages
	 */
	public static final String PING_MESSAGE_ID_KEY = "send_id_key";

	/**
	 * Identifies the message used to share IPs map in the local network
	 */
	public static final int CLIENT_MAP_MESSAGE_TYPE = 1;

	/**
	 * Alert message for debugging purposes
	 */
	public static final int ALERT_MESSAGE_TYPE = 2;
	
	/**
	 * Identifies Hello messages, used to perform range estimation
	 */
	public static final int HELLO_MESSAGE_TYPE = 3;
	
	/**
	 * keys used in getContext of PING_MESSAGE_TYPE messages to address the sender position field  
	 */
	public static final String SENDER_LATITUDE_KEY  = "latitude";
	public static final String SENDER_LONGITUDE_KEY = "longitude";
		
	/**
	 * key used in getContext of PING_MESSAGE_TYPE messages to address the sender transmission range field
	 */
	public static final String SENDER_RANGE_KEY = "range";
	
	public static final String SENDER_DIRECTION_KEY = "direction";
	public static final String MESSAGE_HOP_KEY = "hop_count";
	
	public static final String SENDER_IP_ADDR = "sender_ip";
	
	/****************************************************** METHODS *******************************************/
	
	/**
	 * Generates the message, MUST be called after all content is added in order to properly configure the payload
	 */
	public abstract void prepare();
	public abstract void addContent(String contentKey, String content);
	public abstract Map<String, String> getContent();
	public abstract byte[] getMessage();
	public abstract int getType();
	/**
	 * Returns Sender ID if is available in the message, or null otherwise
	 * 
	 * @return
	 */
	public abstract String getSenderID();
	public abstract String getRecipientAddress();
	public abstract void setRecipientAddress(String recipientAddress);
	
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
