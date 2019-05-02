package org.simplesocks.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.Getter;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 */
@Getter
@Slf4j
public class SimpleSocksProtocolClient   {

	private String host;		//ssocks server host
	private int port;			//ssocks server port
	private String auth;		//ssocks auth
	private EventLoopGroup group;
	//field for auth
	private Channel toRemoteChannel;	//to ssocks server channel
	private GenericFutureListener<? extends Future<? super Channel>> connectionChannelListener;
	private Promise<Channel> connectionChannelPromise;
	private boolean connected = false;		//ssocks connection authed.
	//field for proxy
	private Promise<Channel> proxyChannelPromise;
	private Promise<Void> endProxyPromise;
	private Consumer<ProxyDataRequest> proxyDataRequestConsumer;
	private Consumer<ServerResponse> serverResponseConsumer;
	private Encrypter encrypter = OffsetEncrypter.getInstance();




	public SimpleSocksProtocolClient(String host, int port, String auth, EventLoopGroup group) {
		Objects.requireNonNull(auth);
		this.host = host;
		this.port = port;
		this.group = group;
		this.auth = auth;
	}

	/**
	 * try connecting to remote ssocks server.
	 * @return
	 */
	public void init() {
		try {
			String remoteHost = host;
			int remotePort = port;
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.remoteAddress(remoteHost, remotePort)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new LocalServerChannelInitializer(this));
			bootstrap.connect()
					.addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {

							if (future.isSuccess()) {
								toRemoteChannel = future.channel();
								Promise<Channel> promise = toRemoteChannel.eventLoop().newPromise();
								SimpleSocksProtocolClient.this.connectionChannelPromise = promise;
								if(connectionChannelListener!=null){
									SimpleSocksProtocolClient.this.connectionChannelPromise.addListener(connectionChannelListener);
								}
								log.info("connect to {}:{} success!", remoteHost, remotePort);
							} else {
								throw new BaseSystemException("failed to connect to remote server, cause {}"+ future.cause().getMessage());
							}
						}
					});
		} catch (Exception e) {
			throw new BaseSystemException("Failed to start local server.");
		}
	}


	/**
	 * close client
	 * @throws IOException
	 */

	public void close() {
		endProxy().addListener(future -> {
			clear();
		});
	}

	/**
	 * close client
	 * @throws IOException
	 */

	public void forceClose() {
		if(isConnected()){
			endProxy().addListener(future -> {
				clear();
			});
		}else{
			clear();
		}
	}

	private void clear(){
		if(toRemoteChannel!=null){
			String host = toRemoteChannel.remoteAddress().toString();
			toRemoteChannel.close().addListener(f2->{
				log.info("close connection to server[{}], [{}].", host, f2.isSuccess());
			});
		}
		connected = false;
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
		byte[] decoded = request.getBytes();
		byte[] encoded = encrypter.encode(decoded);
		toRemoteChannel.writeAndFlush(new ProxyDataRequest(encoded)).addListener(future -> {
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

	/**
	 *               ______________
	 *              \|/              |
	 * 			NO_CONNECT ---> CONNECTING --->CONNECTED_IDLE --->PROXY_REQUESTING ---> PROXYING --> END_PROXY_REQUESTING
	 *
	 */
	public enum State{
		NO_CONNECT,
		CONNECTING,
		CONNECTED_IDLE,
		PROXY_REQUESTING,
		PROXYING,
		END_PROXY_REQUESTING,
	}

}
