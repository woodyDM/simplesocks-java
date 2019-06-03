package org.simplesocks.netty.app.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.ConfigurationServer;
import org.simplesocks.netty.app.manager.CompositeRelayClientManager;
import org.simplesocks.netty.app.manager.SimpleSocksRelayClientManager;
import org.simplesocks.netty.common.encrypt.factory.CompositeEncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.util.ServerUtils;

import java.net.BindException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * SOCKS5 local server and SimpleSocks client
 */
@Slf4j
public class LocalSocksServer  {


	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
 	private AppConfiguration configuration;
	private Channel serverChannel;

	public LocalSocksServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, AppConfiguration configuration) {
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		this.configuration = configuration;
	}

	/**
	 * entry
	 */
	public ChannelFuture start() {
		try {
			int port = configuration.getLocalPort();

            /**
             * encType and factory are not so graceful. Refactor later. CompositeEncrypterFactory is desiged to support random authType.
             */
			CompositeEncrypterFactory factory = new CompositeEncrypterFactory();
			factory.registerKey(configuration.getAuth().getBytes(StandardCharsets.UTF_8));
            RelayClientManager manager;
			if(configuration.isGlobalProxy()){
                manager = new SimpleSocksRelayClientManager(configuration.getRemoteHost(),configuration.getRemotePort(),configuration.getAuth(), workerGroup, configuration.getEncryptType(), factory);
            }else{
                manager = new CompositeRelayClientManager(configuration.getRemoteHost(),configuration.getRemotePort(),configuration.getAuth(), workerGroup, configuration.getEncryptType(), factory);
            }
            ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new SocksServerInitializer(manager));
			ChannelFuture channelFuture = bootstrap.bind(port).syncUninterruptibly();
			if(!channelFuture.isSuccess()){
				System.exit(1);
			}
			this.serverChannel = channelFuture.channel();
            log.info("Local proxy server start at port [{}] , mode [{}]."  ,port, configuration.isGlobalProxy()?"GlobalProxyMode":"PacMode");
			return channelFuture;
		} catch (Throwable e) {
		    if(e instanceof BindException){
		        log.error("Failed to start local server, local port[{}] is used by other program.",configuration.getLocalPort(),e);
            }else{
                log.error("Failed to start local server.",e);
            }
		    System.exit(1);
		    return null;
		}
	}


	public void stop(GenericFutureListener<? extends Future<? super Void>> listener){
		if(this.serverChannel!=null){
			serverChannel.close().addListener(listener);
		}else{
			try {
				listener.operationComplete(null);
			} catch (Exception e) {
				throw new IllegalStateException("exception when closing local proxy server.", e);
			}
		}
	}


}
