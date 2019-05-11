package org.simplesocks.netty.server.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 配置
 * 
 *
 * 
 */
@Getter
@Setter
@ToString
public class ServerConfiguration {

	private String auth;
	private int port = 11900;
	private int channelTimeoutSeconds = 600;
	private boolean enableEpoll = false;
	private int initBuffer = 10*1024;

	public ServerConfiguration() {
	}


}
