package org.simplesocks.netty.app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.config.ConfigXmlLoader;
import org.simplesocks.netty.app.manager.CompositeRelayClientManager;
import org.simplesocks.netty.app.manager.SimpleSocksRelayClientManager;
import org.simplesocks.netty.app.proxy.SocksServerInitializer;
import org.simplesocks.netty.common.encrypt.EncType;
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


	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
 	private AppConfiguration configuration;

	public LocalSocksServer(AppConfiguration configuration) {
		this.configuration = configuration;
	}

	public static void main(String[] args) {
		ServerUtils.drawClientStartup(log);
		AppConfiguration configuration = ConfigXmlLoader.load();
		LocalSocksServer server = new LocalSocksServer(configuration);
        Optional<ChannelFuture> future = server.start();
        if(!future.isPresent()){
            server.stop();
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            server.stop();
        }));
        future.get().channel().closeFuture().syncUninterruptibly();
	}


	/**
	 * entry
	 */
	public Optional<ChannelFuture> start() {
		try {
			int port = configuration.getLocalPort();
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
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
            log.info("Local server start At port {} , mode {}."  ,port, configuration.isGlobalProxy()?"GlobalProxyMode":"PacMode");
			return Optional.of(channelFuture);
		} catch (Throwable e) {
		    if(e instanceof BindException){
		        log.error("Failed to start local server, local port[{}] is used by other program.",configuration.getLocalPort(),e);
            }else{
                log.error("Failed to start local server.",e);
            }
		    return Optional.empty();
		}
	}

    /**
     * destroy this server.
     */
	public void stop() {
		ServerUtils.closeEventLoopGroup(bossGroup);
		ServerUtils.closeEventLoopGroup(workerGroup);
		log.info("Stop Server!");
	}



}
