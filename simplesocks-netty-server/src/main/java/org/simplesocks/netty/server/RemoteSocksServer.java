package org.simplesocks.netty.server;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.simplesocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksProtocolEncoder;
import org.simplesocks.netty.server.auth.AuthProvider;
import org.simplesocks.netty.server.auth.MemoryAuthProvider;
import org.simplesocks.netty.server.proxy.ExceptionHandler;
import org.simplesocks.netty.server.proxy.SimpleSocksAuthHandler;
import org.simplesocks.netty.server.proxy.relay.RelayProxyDataHandler;
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


	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap bootstrap = null;

	private static RemoteSocksServer remoteSocksServer = new RemoteSocksServer();

	public static RemoteSocksServer getInstance() {
		return remoteSocksServer;
	}

	public static void main(String[] args) {
		RemoteSocksServer.getInstance().start();
	}


	/**
	 * Main entry
	 */
	public void start() {
		try {
			int port = 10900;

			AuthProvider authProvider = new MemoryAuthProvider();

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
									.addLast(new SimpleSocksAuthHandler(authProvider))
									.addLast(new RelayProxyDataHandler())
									.addLast(new ExceptionHandler(authProvider))
									.addFirst(new SimpleSocksProtocolEncoder());
						}
					});
			logger.info("Remote server start at port {}" ,port);
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
