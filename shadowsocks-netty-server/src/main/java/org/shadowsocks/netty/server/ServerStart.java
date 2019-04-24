package org.shadowsocks.netty.server;

/**
 * socksserver启动类
 * 
 * @author zhaohui
 * 
 */
public class ServerStart {

	public static void main(String[] args) {
		SocksServer.getInstance().start();
	}
}
