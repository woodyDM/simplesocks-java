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
import org.simplesocks.netty.common.encrypt.EncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.encrypt.EncryptInfo;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;

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
	private EventLoopGroup group;	//the group is managed by others, this client does not care about its lifecycle.
	private EncrypterFactory encrypterFactory;

	private EncryptInfo encInfo;	//password
	private ConnectionMessage proxyMessage;

	private Channel toRemoteChannel;	//to ssocks server channel
	private Promise<Channel> connectionPromise;
	private boolean connected = false;

	private Runnable onClose;
	private Consumer<ProxyDataMessage> proxyDataRequestConsumer;

	public SimpleSocksProtocolClient(String auth,String encType, String proxyHost, int proxyPort,  EventLoopGroup group,EncrypterFactory encrypterFactory) {
		Objects.requireNonNull(auth);
		this.auth = auth;
		this.encType = encType;
		this.host = proxyHost;
		this.port = proxyPort;
		this.group = group;
		this.encrypterFactory = encrypterFactory;
	}

	/**
	 *
	 * @return
	 */
	public ChannelFuture init() {
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.remoteAddress(host, port)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new LocalServerChannelInitializer(this));
			return bootstrap.connect()
					.addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							if (future.isSuccess()) {
								toRemoteChannel = future.channel();
								log.debug("connect to s-server[{}:{}] success!", host, port);
							}
						}
					});
		} catch (Throwable e) {
			throw new BaseSystemException("Failed to start local server.", e);
		}
	}

	/**
	 * @return
	 */
	public Promise<Channel> sendProxyRequest(String targetHost, int targetPort, ConnectionMessage.Type proxyType, EventExecutor eventExecutor){
		ConnectionMessage connectionMessage = new ConnectionMessage(auth, encType, targetHost, targetPort, proxyType);
		proxyMessage = connectionMessage;
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


	public void close(){
		if(toRemoteChannel!=null){
			toRemoteChannel.close();
		}
		if(onClose!=null){
		    onClose.run();
        }
	}

	public void onClose(Runnable action){
	    this.onClose = action;
    }

	/**
	 *
	 * @param request
	 */
	public void sendProxyData(ProxyDataMessage request){
		Encrypter encrypter = encrypterFactory.newInstant(encInfo.getType(), encInfo.getIv());
		byte[] plain = request.getData();
		byte[] encrypt = encrypter.encrypt(plain);
		int len = plain.length;
		toRemoteChannel.writeAndFlush(new ProxyDataMessage(request.getId(), encrypt)).addListener(future -> {
			if(!future.isSuccess()){
				log.warn("Failed to send proxy data to remote len={}. cause ", len, future.cause());
				close();
			}
		});
	}



	public void onReceiveProxyData(ProxyDataMessage request){
		if(proxyDataRequestConsumer!=null){
			proxyDataRequestConsumer.accept(request);
		}
	}

	@Override
	public String toString() {
		String target = proxyMessage==null?"":proxyMessage.getHost()+":"+proxyMessage+port;
		return "SimpleSocksProtocolClient{" +
				"Target='" + target + "'encType:"+encType+
				'}';
	}
}
