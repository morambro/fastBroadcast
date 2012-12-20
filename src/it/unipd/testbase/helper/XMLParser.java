package it.unipd.testbase.helper;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLParser {
	
	public static Document xmlToDocument(String xmlSource) throws SAXException, ParserConfigurationException, IOException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    return builder.parse(new InputSource(new StringReader(xmlSource)));
	}
	
	/**
	 * Given an element, returns the value
	 * 
	 * @param ele
	 * @param tagName
	 * @return
	 */
	public static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}
	
	/**
	 * Returns an Element List given a Document and an element name
	 * 
	 * @param doc
	 * @param elementName
	 * @return
	 */
	public static List<Element> getElementFromDoc(Document doc ,String elementName){
		NodeList list = doc.getElementsByTagName(elementName);
		if(list.getLength() == 0){
			return null;
		}else{
			try{
				List<Element> toRet = new ArrayList<Element>();
				for(int i = 0; i<list.getLength();i++)
					if(list.item(0).getNodeType() == Node.ELEMENT_NODE){
						toRet.add((Element)list.item(0));
					}
				return toRet;
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Extracts a value from an XML String given a tag name
	 * 
	 * @param xml
	 * @param tag_name
	 * @return
	 */
	public static String estractTagFromXMLDoc(String xml,String tag_name) {
		try {
			/* Retrieve content */
			Element e = XMLParser.getElementFromDoc(XMLParser.xmlToDocument(xml),"message").get(0);
			
			return XMLParser.getTextValue(e, tag_name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Extracts a vale of an Attribute from an XML String given a tag name
	 * 
	 * @param xml
	 * @param tag_name
	 * @param attribute
	 * @return
	 */
	public static String estractTagAttributeFromXMLDoc(String xml,String tag_name,String attribute) {
		try {
			/* Retrieve content */
			Element e = XMLParser.getElementFromDoc(XMLParser.xmlToDocument(xml),"message").get(0);
			return e.getAttribute(attribute);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Map<String,String> extractDevicesInfo(String xml){
		Document doc;
		
		try{
			doc = xmlToDocument(xml);
		}catch(Exception e){
			return null;
		}
		
		NodeList devices = doc.getElementsByTagName("device");
		Map<String,String> res = new HashMap<String, String>();
		for(int i = 0; i < devices.getLength(); i++){
			Node node = devices.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String client_id = getTagValue("client_id",element);
				String client_ip = getTagValue("client_IP",element);
				res.put(client_id, client_ip);
			}
		}
		return res;
	}
	
	public static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
	 
	        Node nValue = (Node) nlList.item(0);
	 
		return nValue.getNodeValue();
	  }
	
	/**
	 * Method used to get XML from a map of couples <client_id,ip_address>
	 * 
	 * @param map
	 * @return
	 */
	public static String clientMapToString(Map<String,String> map){
		StringBuffer string = new StringBuffer();
		string.append("<message>");
		for(String key : map.keySet()){
			string.append("<element>");
			string.append("<client_id>"+key+"</client_id>");
			string.append("<client_IP>"+map.get(key)+"</client_IP>");
			string.append("</element>");
		}
		string.append("</message>");
		return string.toString();
	}
}
