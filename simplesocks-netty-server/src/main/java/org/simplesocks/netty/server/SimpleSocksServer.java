package org.simplesocks.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.factory.CompositeFactory;
import org.simplesocks.netty.common.netty.SimpleSocksDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksProtocolEncoder;
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;
import org.simplesocks.netty.server.auth.AttributeAuthProvider;
import org.simplesocks.netty.server.proxy.ExceptionHandler;
import org.simplesocks.netty.server.proxy.SimpleSocksAuthHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleSocksServer {


	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
 	private int port;

	public SimpleSocksServer(int port) {
		this.port = port;
	}


	public static void main(String[] args) {
		new SimpleSocksServer(11900).start();
	}


	/**
	 * Main entry
	 */
	public void start() {
		try {
			String pass = "123456笑脸☺";
			CompositeFactory factory = new CompositeFactory();
			factory.registerKey(pass.getBytes(StandardCharsets.UTF_8));
			AuthProvider authProvider = new AttributeAuthProvider(pass);
			int idleSecond = 300;
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup(8);


			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)

					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64,20*1024, 65536))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
                            LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = SimpleSocksDecoder.newLengthDecoder();
                            socketChannel.pipeline()

									.addLast(new IdleStateHandler(idleSecond,idleSecond,idleSecond, TimeUnit.SECONDS))
									.addLast(lengthFieldBasedFrameDecoder)
                                    .addLast(new SimpleSocksProtocolDecoder())
									.addLast(new SimpleSocksAuthHandler(authProvider, factory))
									.addLast(new ExceptionHandler(authProvider))
									.addFirst(new SimpleSocksProtocolEncoder());
						}
					});
			log.info("SSocks server start at port {}" ,port);
			ChannelFuture channelFuture = bootstrap.bind(port);
			channelFuture.sync().channel().closeFuture().sync();
		} catch (Exception e) {
			log.error("start error", e);
		} finally {
			stop();
		}
	}

	public void stop() {
        ServerUtils.closeEventLoopGroup(bossGroup);
        ServerUtils.closeEventLoopGroup(workerGroup);
        log.info("Stop Server!");

	}



}
