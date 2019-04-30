package org.simplesocks.netty.app.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置
 * 
 * @author zhaohui
 * 
 */
public class Config {

	private int localPort;
	private List<RemoteServer> remoteList = new ArrayList<RemoteServer>(20);

	public Config() {

	}

	public void addRemoteConfig(RemoteServer remoteConfig) {
		remoteList.add(remoteConfig);
	}

	public List<RemoteServer> getRemoteList() {
		return remoteList;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

}
