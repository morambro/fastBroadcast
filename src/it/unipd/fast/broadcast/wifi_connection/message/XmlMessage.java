package it.unipd.fast.broadcast.wifi_connection.message;

import it.unipd.fast.broadcast.helper.XMLParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class XmlMessage extends IMessage{
	private static final String TAG = "it.unipd.fast.broadcast";
	private static final String MSG_TAG = "message";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String RECIPIENT_ID_TAG = "recipient_id";
	private static final String CONTENT_BLOCK_TAG = "content_block";
	private static final String CONTENT_TAG = "content";
	private static final String KEY_TAG = "key";
	
	private int type = -1;
	private String message = "";
	private Map<String, String> messageContent = null;
	
	public XmlMessage(int type, String recipientID) {
		this.type = type;
		message += "<"+MSG_TAG+" "+TYPE_ATTRIBUTE+"='"+type+"' "+RECIPIENT_ID_TAG+"='"+recipientID+"'>";
	}
	
	public XmlMessage(String message) {
		this.message = message;
	}
	
	@Override
	public void addContent(String contentKey, String content) {
		if(messageContent==null)
			new HashMap<String, String>();
		if(messageContent == null) messageContent = new HashMap<String, String>();
		messageContent.put(contentKey, content);
	}
	
	@Override
	public Map<String, String> getContent() {
		return MessageParser.getContent(message);
	}
	
	@Override
	public byte[] getMessage() {
		try {
			return message.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getType() {
		if(type==-1)
			type = Integer.valueOf(MessageParser.getType(message));
		return type;
	}

	@Override
	public String getSenderId() {
		if(getType()==PING_MESSAGE_TYPE)
			return getContent().get(PING_MESSAGE_ID_KEY);
		return null;
	}

	@Override
	public String getRecipientAddress() {
		return MessageParser.getRecipientId(message);
	}
	
	@Override
	public void setRecipientAddress(String recipientAddress) {
		String substr = RECIPIENT_ID_TAG+"='";
		int startIndex = message.indexOf(substr)+substr.length()-1;
		int stopIndex = message.substring(startIndex).indexOf("'");
		message = message.substring(0, startIndex)+recipientAddress+message.substring(stopIndex);
		Log.d(TAG, this.getClass().getSimpleName()+": Recipient replaced; "+message);
	}

	@Override
	public void prepare() {
		for (String key : messageContent.keySet()) {
			message +=	"<"+CONTENT_BLOCK_TAG+">" +
						"<"+KEY_TAG+">"+key+"</"+KEY_TAG+">  "+
						"<"+CONTENT_TAG+">"+messageContent.get(key)+"</"+CONTENT_TAG+">" +
						"</"+CONTENT_BLOCK_TAG+">\n";
		}
		message += "</"+MSG_TAG+">";
	}
	
	@Override
	public String toString(){
		return message;
	}
	
	private static class MessageParser {
		private MessageParser(){}
		
		public static String getType(String xml) {
			return XMLParser.estractTagAttributeFromXMLDoc(xml, MSG_TAG, TYPE_ATTRIBUTE);
		}
		
		public static String getRecipientId(String xml) {
			return XMLParser.estractTagAttributeFromXMLDoc(xml, MSG_TAG, RECIPIENT_ID_TAG);
		}
		
		public static Map<String,String> getContent(String xml) {
			Document doc;
			
			try{
				doc = XMLParser.xmlToDocument(xml);
			}catch(Exception e){
				return null;
			}
			
			NodeList devices = doc.getElementsByTagName(CONTENT_BLOCK_TAG);
			Map<String,String> res = new HashMap<String, String>();
			for(int i = 0; i < devices.getLength(); i++){
				Node node = devices.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String client_id = XMLParser.getTagValue(KEY_TAG,element);
					String client_ip = XMLParser.getTagValue(CONTENT_TAG,element);
					res.put(client_id, client_ip);
				}
			}
			return res;
		}
	}
}
