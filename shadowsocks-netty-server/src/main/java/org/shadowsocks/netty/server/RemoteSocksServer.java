package org.shadowsocks.netty.server;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolEncoder;
import org.shadowsocks.netty.server.proxy.SimpleSocksCmdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RemoteSocksServer {

	private static Logger logger = LoggerFactory.getLogger(RemoteSocksServer.class);

	//private static final String CONFIG = "conf/config.xml";

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap bootstrap = null;
	private static RemoteSocksServer remoteSocksServer = new RemoteSocksServer();

	public static RemoteSocksServer getInstance() {
		return remoteSocksServer;
	}

	private RemoteSocksServer() {

	}

	public void start() {
		try {
			int port = 10801;


			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
									1,4,-5,5);
							socketChannel.pipeline()
									.addLast(decoder)
                                    .addLast(new SimpleSocksProtocolDecoder())
									.addLast(new SimpleSocksCmdHandler())
									.addFirst(new SimpleSocksProtocolEncoder());
						}
					});
			logger.info("Start At Port {} " ,port);
			bootstrap.bind(port).sync().channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			stop();
		}
	}

    public static void main(String[] args) {
        RemoteSocksServer.getInstance().start();
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
