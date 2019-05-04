package org.simplesocks.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;
import org.simplesocks.netty.common.protocol.ServerResponseMessage;

import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 */
@Getter
@Setter
@Slf4j
public class SimpleSocksProtocolClient   {


	private String auth;		//ssocks auth
	private String encType;
	private String host;		//ssocks server host
	private int port;			//ssocks server port

	private String encPassword;	//password
	private EventLoopGroup group;

	private Channel toRemoteChannel;	//to ssocks server channel
	private Promise<Channel> connectionPromise;
	private boolean connected = false;

	private Consumer<ProxyDataMessage> proxyDataRequestConsumer;
	private Consumer<ServerResponseMessage> serverResponseConsumer;
	private Encrypter encrypter = OffsetEncrypter.getInstance();




	public SimpleSocksProtocolClient(String auth,String encType, String proxyHost, int proxyPort,  EventLoopGroup group) {
		Objects.requireNonNull(auth);
		this.auth = auth;
		this.encType = encType;
		this.host = proxyHost;
		this.port = proxyPort;
		this.group = group;
	}

	/**
	 *
	 * @return
	 */
	public ChannelFuture init() {
		try {
			String remoteHost = host;
			int remotePort = port;
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.remoteAddress(remoteHost, remotePort)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new LocalServerChannelInitializer(this));
			return bootstrap.connect()
					.addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							if (future.isSuccess()) {
								toRemoteChannel = future.channel();
								log.debug("connect to {}:{} success!", remoteHost, remotePort);
							}
						}
					});
		} catch (Exception e) {
			throw new BaseSystemException("Failed to start local server.");
		}
	}

	/**
	 * @return
	 */
	public Promise<Channel> sendProxyRequest(String targetHost, int targetPort, ConnectionMessage.Type proxyType, EventExecutor eventExecutor){
		ConnectionMessage connectionMessage = new ConnectionMessage(auth, encType, targetHost, targetPort, proxyType);
		Promise<Channel> promise = eventExecutor.newPromise();
		this.connectionPromise = promise;
		toRemoteChannel.writeAndFlush(connectionMessage).addListener(f->{
			if(!f.isSuccess()){
				promise.setFailure(new BaseSystemException("failed to send proxy request to remote!"));
			}
		});
		return promise;
	}

	public void setProxyDataRequestConsumer(Consumer<ProxyDataMessage> proxyDataRequestConsumer) {
		this.proxyDataRequestConsumer = proxyDataRequestConsumer;
	}

	public void setServerResponseConsumer(Consumer<ServerResponseMessage> serverResponseConsumer) {
		this.serverResponseConsumer = serverResponseConsumer;
	}

	public void close(){
		if(toRemoteChannel!=null){
			toRemoteChannel.close();
		}
	}


	/**
	 *
	 * @param request
	 */
	public void sendProxyData(ProxyDataMessage request){
		byte[] decoded = request.getData();
		byte[] encoded = encrypter.encode(decoded);
		toRemoteChannel.writeAndFlush(new ProxyDataMessage(request.getId(), encoded)).addListener(future -> {
			if(!future.isSuccess()){
				//log.warn("Failed to send proxy data to remote, closing client.");
				//close();
			}
		});
	}



	public void onReceiveProxyData(ProxyDataMessage request){
		if(proxyDataRequestConsumer!=null){
			proxyDataRequestConsumer.accept(request);
		}
	}

	public void onReceiveServerResponse(ServerResponseMessage response){
		if(serverResponseConsumer!=null){
			serverResponseConsumer.accept(response);
		}
	}


}
