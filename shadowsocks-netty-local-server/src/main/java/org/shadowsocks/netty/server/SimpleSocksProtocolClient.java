package org.shadowsocks.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;
import org.shadowsocks.netty.common.protocol.ProxyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SimpleSocksProtocolClient {

	private static Logger logger = LoggerFactory.getLogger(SimpleSocksProtocolClient.class);

	//private static final String CONFIG = "conf/config.xml";
	private EventLoopGroup group;
	private Bootstrap bootstrap;
	private String host;
	private int port;
	private String auth;
	private boolean selfManageEventLoop;
	private Channel toRemoteChannel;
	private Promise<Boolean> connectAction;
	private boolean connected = false;

	public SimpleSocksProtocolClient(String host, int port, String auth) {
		Objects.requireNonNull(auth);
		this.host = host;
		this.port = port;
		selfManageEventLoop = true;
		this.auth = auth;
		group = new NioEventLoopGroup();
	}

	public SimpleSocksProtocolClient(String host, int port, String auth, EventLoopGroup group) {
		Objects.requireNonNull(auth);
		this.host = host;
		this.port = port;
		this.group = group;
		this.auth = auth;
		selfManageEventLoop = false;
	}


	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getAuth() {
		return auth;
	}

	public Channel getToRemoteChannel() {
		return toRemoteChannel;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public Promise<Boolean> getConnectAction() {
		return connectAction;
	}

	public void setConnectAction(Promise<Boolean> connectAction) {
		this.connectAction = connectAction;
	}

	public void init() {
		try {
			String remoteHost = host;
			int remotePort = port;
			bootstrap = new Bootstrap();
			bootstrap.group(group)
					.remoteAddress(remoteHost, remotePort)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new LocalServerChannelInitializer(this));
			ChannelFuture channelFuture = bootstrap.connect()
					.addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							if (future.isSuccess()) {
								toRemoteChannel = future.channel();
								logger.info("connect to {}:{} success!", remoteHost, remotePort);
							} else {
								logger.error("failed connect. cause: {}", future.cause().getMessage());
							}
						}
					});
			if(selfManageEventLoop){
				channelFuture.channel().closeFuture().sync();
			}
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			stop();
		}
	}

	public void stop() {
		if (group != null&& selfManageEventLoop) {
			group.shutdownGracefully();
		}
		if(toRemoteChannel!=null){
			toRemoteChannel.close();
		}
	}


	public Promise<Void> sendProxyRequest(String host, int port, ProxyRequest.Type type){
		if(!connected || toRemoteChannel==null||!toRemoteChannel.isActive()){
			throw new IllegalStateException("broken client from pool.");
		}
		ProxyRequest proxyRequest = new ProxyRequest(type, port, host);

		toRemoteChannel.writeAndFlush(proxyRequest);



	}

}
