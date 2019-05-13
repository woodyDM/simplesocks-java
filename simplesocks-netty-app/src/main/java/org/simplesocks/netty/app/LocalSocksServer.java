package org.simplesocks.netty.app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.manager.CompositeRelayClientManager;
import org.simplesocks.netty.app.manager.SimpleSocksRelayClientManager;
import org.simplesocks.netty.app.proxy.SocksServerInitializer;
import org.simplesocks.netty.common.encrypt.EncType;
import org.simplesocks.netty.common.encrypt.factory.CompositeEncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.util.ServerUtils;

import java.nio.charset.StandardCharsets;

/**
 * SOCKS5 本地代理
 */
@Slf4j
public class LocalSocksServer  {

	private static final String CONFIG = "conf/config.xml";
	private static final String PAC = "conf/pac.xml";

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
 	private AppConfiguration configuration;

	public LocalSocksServer(AppConfiguration configuration) {
		this.configuration = configuration;
	}

	public static void main(String[] args) {
		AppConfiguration configuration = new AppConfiguration();
        configuration.setLocalPort(10800);
        configuration.setEncryptType(EncType.AES_CBC.getEncName());
        configuration.setRemoteHost("35.229.240.146");
        configuration.setRemotePort(12000);
        configuration.setAuth("1234567");
		LocalSocksServer server = new LocalSocksServer(configuration);
        ChannelFuture future = server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            server.stop();
        }));
        future.channel().closeFuture().syncUninterruptibly();
	}

	/**
	 * 入口
	 */

	public ChannelFuture start() {
		try {
			int port = configuration.getLocalPort();
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
            //encType and factory not graceful. Refactor later. CompositeEncrypterFactory is desiged to support random authType.
			CompositeEncrypterFactory factory = new CompositeEncrypterFactory();
			factory.registerKey(configuration.getAuth().getBytes(StandardCharsets.UTF_8));
            RelayClientManager manager;
			if(configuration.isForceProxy()){
                manager = new SimpleSocksRelayClientManager(configuration.getRemoteHost(),configuration.getRemotePort(),configuration.getAuth(), workerGroup, configuration.getEncryptType(), factory);
            }else{
                manager = new CompositeRelayClientManager(configuration.getRemoteHost(),configuration.getRemotePort(),configuration.getAuth(), workerGroup, configuration.getEncryptType(), factory);
            }
            ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new SocksServerInitializer(manager));
			ChannelFuture channelFuture = bootstrap.bind(port).syncUninterruptibly();
            log.info("Local server start At port {} , mode {}."  ,port, configuration.isForceProxy()?"ForceProxy":"PacMode");
			return channelFuture;
		} catch (Throwable e) {
			throw new BaseSystemException("Failed to start local server.", e);
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
