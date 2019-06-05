package org.simplesocks.netty.app.config;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.EncType;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.util.ConfigPathUtil;
import org.simplesocks.netty.common.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 *
 *
 *
 */
@Slf4j
public class ConfigXmlLoader {

	public static AppConfiguration load(String path) throws FileNotFoundException  {
		InputStream in = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			String fullPath = ConfigPathUtil.getUserDirFullName(path);
			log.info("Loading config from {}", fullPath);
			in = new FileInputStream(fullPath);
			Document doc = builder.parse(in);
            AppConfiguration config = new AppConfiguration();

            NodeList list = doc.getElementsByTagName(Constants.XML_ROOT);

			if (list.getLength() > 0) {
				Node node = list.item(0);
				NodeList nodes = node.getChildNodes();

				for (int i = 0; i < nodes.getLength(); i++) {
                    String nodeName = nodes.item(i).getNodeName();
                    String value = StringUtils.trim(nodes.item(i).getTextContent());
                    if (Constants.XML_LOCAL_PORT.equalsIgnoreCase(nodeName)) {
                        config.configureLocalPort(Integer.parseInt(value));
                    } else if (Constants.XML_LOCAL_SERVER_PORT.equalsIgnoreCase(nodeName)) {
                        config.configureLocalConfigServerPort(Integer.parseInt(value));
                    } else if (Constants.XML_AUTH.equalsIgnoreCase(nodeName)){
                        config.configureAuth(value);
                    } else if (Constants.XML_REMOTE_PORT.equalsIgnoreCase(nodeName)) {
                        config.configureRemotePort(Integer.parseInt(value));
                    } else if (Constants.XML_REMOTE_HOST.equalsIgnoreCase(nodeName)) {
                        config.configureRemoteHost(value);
                    } else if (Constants.XML_GLOBAL_TYPE.equalsIgnoreCase(nodeName)) {
                        config.configureGlobalProxy(value);
                    } else if (Constants.XML_ENCRYPT_TYPE.equalsIgnoreCase(nodeName)) {
                        boolean typeSupports = Arrays.stream(EncType.values())
                                .anyMatch(t -> t.getEncName().equalsIgnoreCase(value));
                        if(typeSupports)
                            config.configureEncryptType(value);
                        else{
                            String supportsTypes = Arrays.stream(EncType.values())
                                    .map(t -> "[" + t.getEncName() + "]")
                                    .collect(Collectors.joining(","));
                            throw new IllegalArgumentException("encryptType only supports "+supportsTypes);
                        }
                    }else if(Constants.XML_WHITE_LIST.equalsIgnoreCase(nodeName)){
                        NodeList childNodes = nodes.item(i).getChildNodes();
                        parseSites(config, childNodes, true);
                    }else if(Constants.XML_PROXY_LIST.equalsIgnoreCase(nodeName)){
                        NodeList childNodes = nodes.item(i).getChildNodes();
                        parseSites(config, childNodes, false);
                    }
                }
                log.info("load config complete {}!", config);
            }else{
			    log.warn("Invalid config.xml, check your configuration.");
            }
            if(config.getAuth()==null){
                throw new IllegalArgumentException("Invalid config.xml, must provide [auth] to server.");
            }
            return config;
		} catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseSystemException("Failed to load config file. check your conf/config.xml.",e);
        } finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {//ignore
				}
			}
		}
	}


	private static void parseSites(AppConfiguration configuration, NodeList childNodes, boolean isWhiteList){
        int len = childNodes.getLength();
        for (int i = 0; i < len; i++) {
            Node item = childNodes.item(i);
            if(Constants.XML_SITE.equalsIgnoreCase(item.getNodeName())){
                String nodeValue = StringUtils.trim(item.getTextContent());
                if(isWhiteList)
                    configuration.addWhiteSite(nodeValue);
                else
                    configuration.addProxySite(nodeValue);
            }
        }
    }
}
