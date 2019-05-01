package org.simplesocks.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.simplesocks.netty.common.protocol.EndProxyRequest;
import org.simplesocks.netty.common.protocol.ProxyDataRequest;
import org.simplesocks.netty.common.protocol.ProxyRequest;
import org.simplesocks.netty.common.protocol.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 */
@Getter
@Slf4j
public class SimpleSocksProtocolClient   {

	private static Logger logger = LoggerFactory.getLogger(SimpleSocksProtocolClient.class);


	private EventLoopGroup group;
	private Bootstrap bootstrap;
	private String host;		//ssocks server host
	private int port;			//ssocks server port
	private String auth;		//ssocks auth
	private boolean selfManageEventLoop;
	private Channel toRemoteChannel;	//to ssocks server channel
	private GenericFutureListener<? extends Future<? super Channel>> connectionChannelListener;
	private Promise<Channel> connectionChannelPromise;
	private boolean connected = false;		//ssocks connection connected.
	//field for proxy
	private Promise<Channel> proxyChannelPromise;
	private Promise<Void> endProxyPromise;
	private Consumer<ProxyDataRequest> proxyDataRequestConsumer;
	private Consumer<ServerResponse> serverResponseConsumer;

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

	/**
	 * try connecting to remote ssocks server.
	 * @return
	 */
	public ChannelFuture init() {
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
								createPromise(toRemoteChannel);
								logger.info("connect to {}:{} success!", remoteHost, remotePort);
							} else {
								logger.error("failed connect. cause: {}", future.cause().getMessage());
							}
						}
					});
			if(selfManageEventLoop){
				channelFuture.channel().closeFuture().sync();
			}
			return channelFuture;
		} catch (Exception e) {
			throw new RuntimeException("Failed to start local server.");
		} finally {
			if(selfManageEventLoop){
				close();
			}
		}
	}


	/**
	 * close client
	 * @throws IOException
	 */

	public void close() {
		if (group != null && selfManageEventLoop) {
			group.shutdownGracefully();
		}
		endProxy().addListener(future -> {
			if(toRemoteChannel!=null){
				toRemoteChannel.close().addListener(f2->{
					log.info("close connection to ssocks server, result is {}.", f2.isSuccess());
				});
			}
			connected = false;
		});
	}


	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	/**
	 * must call before init.
	 * @param connectionChannelListener
	 */
	public void setConnectionChannelListener(GenericFutureListener<? extends Future<? super Channel>> connectionChannelListener){
		this.connectionChannelListener = connectionChannelListener;
	}

	public void setProxyDataRequestConsumer(Consumer<ProxyDataRequest> proxyDataRequestConsumer) {
		this.proxyDataRequestConsumer = proxyDataRequestConsumer;
	}

	public void setServerResponseConsumer(Consumer<ServerResponse> serverResponseConsumer) {
		this.serverResponseConsumer = serverResponseConsumer;
	}

	private void createPromise(Channel channel){
		this.connectionChannelPromise = channel.eventLoop().newPromise();
		if(connectionChannelListener!=null){
			this.connectionChannelPromise.addListener(connectionChannelListener);
		}
	}

	/**
	 *
	 * @param host
	 * @param port
	 * @param proxyType
	 * @return
	 */
	public Promise<Channel> sendProxyRequest(String host, int port, ProxyRequest.Type proxyType){
		if(!connected || toRemoteChannel==null||!toRemoteChannel.isActive()){
			throw new IllegalStateException("broken client, too early or the client is closed.");
		}
		if(proxyChannelPromise!=null){
			throw new IllegalStateException("before a new proxy request ,try end previous proxy.");
		}
		ProxyRequest proxyRequest = new ProxyRequest(proxyType, port, host);
		proxyChannelPromise = toRemoteChannel.eventLoop().newPromise();
		toRemoteChannel.writeAndFlush(proxyRequest).addListener(f->{
			if(!f.isSuccess()){
				proxyChannelPromise.setFailure(new RuntimeException("failed to send proxy request to remote!"));
			}
		});
		return proxyChannelPromise;
	}

	/**
	 *
	 * @param request
	 */
	public void sendProxyData(ProxyDataRequest request){
		toRemoteChannel.writeAndFlush(request).addListener(future -> {
			if(!future.isSuccess()){
				log.warn("failed to send proxy data to remote.");
			}
		});
	}


	public Promise<Void> endProxy(){
		if(proxyChannelPromise==null){
			throw new IllegalStateException("Can't end proxy because there is no proxy task.");
		}
		Promise<Void> promise = toRemoteChannel.eventLoop().newPromise();
		this.endProxyPromise = promise;
		toRemoteChannel.writeAndFlush(new EndProxyRequest());
		return promise;
	}


	public void clearProxySession(){
		this.endProxyPromise = null;
		this.proxyChannelPromise = null;
	}

	public void onReceiveProxyData(ProxyDataRequest request){
		if(proxyDataRequestConsumer!=null){
			proxyDataRequestConsumer.accept(request);
		}
	}

	public void onReceiveServerResponse(ServerResponse response){
		if(serverResponseConsumer!=null){
			serverResponseConsumer.accept(response);
		}
	}


}
