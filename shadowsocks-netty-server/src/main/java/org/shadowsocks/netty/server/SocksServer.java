package org.shadowsocks.netty.server;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.shadowsocks.netty.common.netty.ByteReceiveHandler;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.shadowsocks.netty.common.netty.SimpleSocksProtocolEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SocksServer {

	private static Logger logger = LoggerFactory.getLogger(SocksServer.class);

	//private static final String CONFIG = "conf/config.xml";

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap bootstrap = null;
	private static SocksServer socksServer = new SocksServer();

	public static SocksServer getInstance() {
		return socksServer;
	}

	private SocksServer() {

	}

	public void start() {
		try {
			int port = 10801;

			LengthFieldBasedFrameDecoder decoder = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
					1,4,-5,5);
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline()
									.addLast(decoder)
                                    .addLast(new SimpleSocksProtocolDecoder())
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
        SocksServer.getInstance().start();
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
