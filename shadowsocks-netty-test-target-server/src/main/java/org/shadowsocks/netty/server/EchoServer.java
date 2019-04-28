package org.shadowsocks.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServer {

	private static Logger logger = LoggerFactory.getLogger(EchoServer.class);

	//private static final String CONFIG = "conf/config.xml";

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap bootstrap = null;
	private static EchoServer echoServer = new EchoServer();

	public static EchoServer getInstance() {
		return echoServer;
	}

	public static final int PORT = 8086;


    public static void main(String[] args) {
        EchoServer.getInstance().start();
    }


    public void start() {
		try {
			int port = PORT;
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(new EchoHandler());
			logger.info("Target Server [Echo] start at port {} " ,port);
			bootstrap.bind(port).sync().channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			stop();
		}
	}

	public void stop() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
		logger.info("Stop Server!");
	}

}
