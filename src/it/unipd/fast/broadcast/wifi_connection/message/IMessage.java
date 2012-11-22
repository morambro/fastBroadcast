package it.unipd.fast.broadcast.wifi_connection.message;

import java.util.Map;

//Interfaccia per i messaggi scambiati tra i peers
public interface IMessage {
	
	public static final String BROADCAST_ADDRESS = "255.255.255.255";
	//Hello message used to collect IP addresses
	public static final int PING_MESSAGE_TYPE = 0;
	//key used in getContext of PING_MESSAGE_TYPE messages to address the sender ID field
	public static final String PING_MESSAGE_ID_KEY = "send_id_key";

	//Message used to share IP address of the network peers
	public static final int CLIENT_MAP_MESSAGE_TYPE = 1;

	//Message used to send an ALERT message
	public static final int ALERT_MESSAGE_TYPE = 2;
	
	//Fast Broadcast Protocol Hello Message
	public static final int HELLO_MESSAGE_TYPE = 3;
	//key used in getContext of PING_MESSAGE_TYPE messages to address the sender position field  
	public static final String HELLO_SENDER_POS_KEY = "send_pos_map_key";
	//key used in getContext of PING_MESSAGE_TYPE messages to address the sender transmission range field
	public static final String HELLO_SENDER_RANGE_KEY = "send_range_map_key";
	
	public void addContent(String contentKey, String content);
	public Map<String, String> getContent();
	public byte[] getMessage();
	public int getType();
	public String getSenderId();
	public String getRecipientAddress();
	public void setRecipientAddress(String recipientAddress);
	//generates the message, MUST be called after all content is added in order to properly configure the payload
	public void prepare();
}
