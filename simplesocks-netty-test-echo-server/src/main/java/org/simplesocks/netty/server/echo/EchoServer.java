package org.simplesocks.netty.server.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServer {

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;


	public static final int PORT = 8086;


    public static void main(String[] args) {
        new EchoServer().start();
    }

    public void start() {
		try {
			int port = PORT;
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(new EchoHandler());
			log.info("Target Server [Echo] start at port {} " ,port);
			bootstrap.bind(port).sync().channel().closeFuture().sync();
		} catch (Exception e) {
			log.error("start error", e);
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
		log.info("Stop Server!");
	}

}
