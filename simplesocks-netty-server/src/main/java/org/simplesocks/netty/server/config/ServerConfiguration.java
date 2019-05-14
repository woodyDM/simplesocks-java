package org.simplesocks.netty.server.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 
 *
 * 
 */
@Getter
@ToString
@Slf4j
public class ServerConfiguration {

	private String auth;
	private int port = 12020;
	private int channelTimeoutSeconds = 600;
	private boolean enableEpoll = false;
	private boolean propertyExist = false;
	private int initBuffer = 10*1024;

	private static final int MAX_BUFFER = 30*1024;
	private static final int MIN_BUFFER = 128;

	public ServerConfiguration(String auth) {
		if(auth==null||auth.length()==0)
			throw new IllegalArgumentException("auth should not be empty.");
		this.auth = auth;
		String property = System.getProperty(Constants.PROPERTY_KEY);
		if("true".equals(property)){
			this.enableEpoll = true;
			this.propertyExist = true;
		}else if("false".equals(property)){
			this.enableEpoll = false;
			this.propertyExist = true;
		}else{
			log.debug("Property [{}] invalid, ignore!", Constants.PROPERTY_KEY);
		}
		if(propertyExist)
			log.debug("Property [{}] is {}.", Constants.PROPERTY_KEY, enableEpoll);
	}


	void configAuth(String auth){
		this.auth = auth;
	}

	public void configPort(int port) {
		check("port", port);
		this.port = port;
	}

	public void configChannelTimeoutSeconds(int channelTimeoutSeconds) {
		check("channelTimeoutSeconds",channelTimeoutSeconds);
		this.channelTimeoutSeconds = channelTimeoutSeconds;
	}



	public void configEnableEpoll(boolean enableEpoll) {
		if(!propertyExist)
			this.enableEpoll = enableEpoll;
	}

	public void configInitBuffer(int initBuffer) {
		check("initBuffer",initBuffer);
		if(initBuffer>MAX_BUFFER){
			this.initBuffer = MAX_BUFFER;
		}else if(initBuffer<MIN_BUFFER){
			this.initBuffer = MIN_BUFFER;
		}else{
			this.initBuffer = initBuffer;
		}
	}

	private void check(String field, int value){
		if(value<=0)
			throw new IllegalArgumentException(field+" should > 0");
	}
}
