package org.simplesocks.netty.server.config;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.util.ConfigPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 *
 *
 *
 *
 */
@Slf4j
public class ConfigurationXmlLoader {


	public static Optional<ServerConfiguration> load(String relativePath)  {

		String fullPath = ConfigPathUtil.getUserDirFullName(relativePath);
		try(InputStream in = new FileInputStream(fullPath)){
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(in);
			NodeList list = doc.getElementsByTagName(Constants.XML_ROOT);
			ServerConfiguration serverConfiguration = new ServerConfiguration("TEMP");
			String realAuth = null;
			if (list.getLength() > 0) {
				Node node = list.item(0);
				NodeList cnodes = node.getChildNodes();

				for (int i = 0; i < cnodes.getLength(); i++) {
					String nodeName = cnodes.item(i).getNodeName();
					String content = cnodes.item(i).getTextContent();
					if (Constants.XML_AUTH.equals(nodeName)) {
						if(content==null||content.isEmpty())
							return Optional.empty();
						realAuth = content;
						serverConfiguration.configAuth(realAuth);
					} else if (Constants.XML_PORT.equals(nodeName)) {
						serverConfiguration.configPort(Integer.valueOf(content));
					} else if (Constants.XML_CHANNEL_TIMEOUT_SECONDS.equals(nodeName)) {
						serverConfiguration.configChannelTimeoutSeconds(Integer.valueOf(content));
					}else if (Constants.XML_INIT_BUFFER.equals(nodeName)) {
						serverConfiguration.configInitBuffer(Integer.valueOf(content));
					}else if (Constants.XML_ENABLE_EPOLL.equals(nodeName)) {
						if("true".equalsIgnoreCase(content)){
							serverConfiguration.configEnableEpoll(true);
						}else if("false".equalsIgnoreCase(content)){
							serverConfiguration.configEnableEpoll(false);
						}else{
							return Optional.empty();
						}
					}
				}
				if(realAuth==null)	//no auth found!
					return Optional.empty();
				else
					return Optional.of(serverConfiguration);
			}else{
				return Optional.empty();
			}
		} catch (Exception e) {
			log.error("Xml parse error,check your configuration file at conf/config.xml.", e);
			return Optional.empty();
		}
	}
}
