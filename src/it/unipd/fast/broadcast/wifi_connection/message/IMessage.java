package it.unipd.fast.broadcast.wifi_connection.message;

import java.util.Map;

/**
 * Message interface
 * 
 * @author Fabio De Gaspari
 *
 */
public interface IMessage {
	/**
	 * Address used to identify broadcast messages
	 */
	public static final String BROADCAST_ADDRESS = "255.255.255.255";
	
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
	
	public static final String HELLO_SENDER_DIRECTION_KEY = "direction";
	
	/****************************************************** METHODS *******************************************/
	
	public void addContent(String contentKey, String content);
	public Map<String, String> getContent();
	public byte[] getMessage();
	public int getType();
	public String getSenderId();
	public String getRecipientAddress();
	public void setRecipientAddress(String recipientAddress);
	/**
	 * Generates the message, MUST be called after all content is added in order to properly configure the payload
	 */
	public void prepare();
}
