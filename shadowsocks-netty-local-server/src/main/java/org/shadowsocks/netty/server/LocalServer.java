package org.shadowsocks.netty.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServer {

	private static Logger logger = LoggerFactory.getLogger(LocalServer.class);

	//private static final String CONFIG = "conf/config.xml";

	private EventLoopGroup group = null;
	private Bootstrap bootstrap = null;
	private static LocalServer localServer = new LocalServer();

	public static LocalServer getInstance() {
		return localServer;
	}



    public static void main(String[] args) {
        LocalServer.getInstance().start();
    }


    public void start() {
		try {
			String remoteHost = "localhost";
			int remotePort = 10900;
			group = new NioEventLoopGroup( );
			bootstrap = new Bootstrap();
			bootstrap.group(group)
					.remoteAddress(remoteHost, remotePort)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new LocalServerChannelInitializer());
			ChannelFuture channelFuture = bootstrap.connect()
					.addListener(future -> {
						if (future.isSuccess()) {
							logger.info("connect to {}:{} success!", remoteHost, remotePort);
						} else {
							logger.error("failed connect. cause: {}", future.cause().getMessage());
						}
					});
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			stop();
		}
	}

	public void stop() {
		if (group != null) {
			group.shutdownGracefully();
		}
		logger.info("Stop Server!");
	}

}
