//package org.shadowsocks.netty.common.encrypt;
//
//import java.lang.reflect.Constructor;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.shadowsocks.netty.common.encrypt.impl.AesCrypt;
//import org.shadowsocks.netty.common.encrypt.impl.BlowFishCrypt;
//import org.shadowsocks.netty.common.encrypt.impl.CamelliaCrypt;
//import org.shadowsocks.netty.common.encrypt.impl.SeedCrypt;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class CryptFactory {
//
//	private static Logger logger = LoggerFactory.getLogger(CryptFactory.class);
//
//	private static Map<String, String> crypts = new HashMap<String, String>();
//
//	static {
//		crypts.putAll(AesCrypt.getCipherImplements());
//		crypts.putAll(CamelliaCrypt.getCipherImplements());
//		crypts.putAll(BlowFishCrypt.getCipherImplements());
//		crypts.putAll(SeedCrypt.getCipherImplements());
//	}
//
//	public static ICrypt get(String name, String password) {
//		String className = crypts.get(name);
//		if (className == null) {
//			return null;
//		}
//
//		try {
//			Class<?> clazz = Class.forName(className);
//			Constructor<?> constructor = clazz.getConstructor(String.class, String.class);
//			return (ICrypt) constructor.newInstance(name, password);
//		} catch (Exception e) {
//			logger.error("get crypt error", e);
//		}
//
//		return null;
//	}
//}
