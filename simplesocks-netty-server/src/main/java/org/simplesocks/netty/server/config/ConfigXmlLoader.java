//package org.simplesocks.netty.server.config;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * 加载Config配置xml
// *
// * @author zhaohui
// *
// */
//@Deprecated
//public class ConfigXmlLoader {
//
//	private static Logger logger = LoggerFactory.getLogger(ConfigXmlLoader.class);
//
//	public static ServerConfiguration load(String file) throws Exception {
//		InputStream in = null;
//		try {
//			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
//					.newDocumentBuilder();
//			in = new FileInputStream(file);
//			Document doc = builder.parse(in);
//			NodeList list = doc.getElementsByTagName("serverConfiguration");
//
//			ServerConfiguration serverConfiguration = new ServerConfiguration();
//			if (list.getLength() > 0) {
//				Node node = list.item(0);
//				NodeList childs = node.getChildNodes();
//
//				for (int j = 0; j < childs.getLength(); j++) {
//					if ("ip_addr".equals(childs.item(j).getNodeName())) {
//						serverConfiguration.set_ipAddr(childs.item(j).getTextContent());
//					} else if ("port".equals(childs.item(j).getNodeName())) {
//						serverConfiguration.set_port(Integer.parseInt(childs.item(j)
//								.getTextContent()));
//					} else if ("local_ip_addr".equals(childs.item(j)
//							.getNodeName())) {
//						serverConfiguration.set_localIpAddr(childs.item(j).getTextContent());
//					} else if ("local_port"
//							.equals(childs.item(j).getNodeName())) {
//						serverConfiguration.set_localPort(Integer.parseInt(childs.item(j)
//								.getTextContent()));
//					} else if ("method".equals(childs.item(j).getNodeName())) {
//						serverConfiguration.set_method(childs.item(j).getTextContent());
//					} else if ("password".equals(childs.item(j).getNodeName())) {
//						serverConfiguration.set_password(childs.item(j).getTextContent());
//					}
//				}
//			}
//			logger.info("load serverConfiguration !");
//			return serverConfiguration;
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//	}
//}
