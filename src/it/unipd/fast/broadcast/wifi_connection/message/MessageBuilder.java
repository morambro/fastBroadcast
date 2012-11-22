package it.unipd.fast.broadcast.wifi_connection.message;


public class MessageBuilder {
	private static MessageBuilder singleton = null;

	private MessageBuilder(){}

	public static MessageBuilder getInstance() {
		if(singleton==null)
			singleton = new MessageBuilder();
		return singleton;
	}
	
	public IMessage getMessage(String message) {
		return createMessage(message);
	}

	public IMessage getMessage(int type, String recipientID) {
		return createMessage(type, recipientID);
	}
	public IMessage getMessage(int type, String recipientID, String contentKey, String content) {
		IMessage temp = getMessage(type, recipientID);
		temp.addContent(contentKey, content);
		temp.prepare();
		return temp;
	}
	
	private IMessage createMessage(int type, String recipientID) {
		return new XmlMessage(type, recipientID);
	}
	
	private IMessage createMessage(String message) {
		return new XmlMessage(message);
	}
}
