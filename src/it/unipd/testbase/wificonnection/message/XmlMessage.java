package it.unipd.testbase.wificonnection.message;

import it.unipd.testbase.helper.XMLParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlMessage extends IMessage{
	private static final String TAG = "it.unipd.testbase";
	private static final String MSG_TAG = "message";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String CONTENT_BLOCK_TAG = "content_block";
	private static final String CONTENT_TAG = "content";
	private static final String KEY_TAG = "key";
	private static final String APP_ID_ATTRIBUTE = "application_id";

	private int type = -1;
	private String message = "";
	private Map<String, String> messageContent = null;

	@Override
	public void __debug_set_message(String message)
	{
		this.message = message;
	}

	@Override
	public String __debug_get_message() {
		return message;
	}


	public XmlMessage(int type, String recipientID,String senderIP) {
		this.type = type;
		message += "<"+MSG_TAG+" "+TYPE_ATTRIBUTE+"='"+type+"' "+APP_ID_ATTRIBUTE+"='"+recipientID+"'>";
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
	public String getMessageString() {
		return message;
	}

	@Override
	public int getType() {
		if(type==-1)
			type = Integer.valueOf(MessageParser.getType(message));
		return type;
	}

	@Override
	public int getAppID() {
		String s = MessageParser.getAttribute(message, APP_ID_ATTRIBUTE);
		if(s.equals(""))
			return -1;
		else
			return Integer.valueOf(s);
	}


	@Override
	public void prepare() {
		if(messageContent!=null)
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

		public static String getAttribute(String xml, String attribute) {
			return XMLParser.estractTagAttributeFromXMLDoc(xml, MSG_TAG, attribute);
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

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof IMessage))
			return false;
		IMessage o2 = (IMessage) o;
		if(o2.getType() != this.getType())
			return false;
		return true;
	}
}