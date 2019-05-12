package org.simplesocks.netty.app.gfw;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.util.ConfigPathUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * 
 *
 * 
 */
@Slf4j
public class PacXmlLoader {


	public static void main(String[] args) {
		Set<String> strings = loadPacSites();
		System.out.println(".");
	}

	public static Set<String> loadPacSites( )  {
		Set<String> result = new HashSet<>(4500);
		String file = ConfigPathUtil.getUserDirFullName(Constants.PATH);
		log.info("Load pac setting from {}.",file);
		InputStream in = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			in = new FileInputStream(file);

			Document doc = builder.parse(in);
			NodeList list = doc.getElementsByTagName(Constants.XML_ROOT);

			if (list.getLength() > 0) {
				NodeList childNodes = list.item(0).getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					String nodeName = childNodes.item(i).getNodeName();
					if(Constants.XML_NODE.equalsIgnoreCase(nodeName)){
						result.add(childNodes.item(i).getTextContent());
					}
				}
			}
			return result;

		} catch (Exception e) {
			throw new BaseSystemException("Failed to load pac list.",e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
	}


}
