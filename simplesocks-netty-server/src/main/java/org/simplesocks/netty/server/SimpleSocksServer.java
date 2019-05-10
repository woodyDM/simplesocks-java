package org.simplesocks.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.factory.CompositeEncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.SimpleSocksDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksProtocolDecoder;
import org.simplesocks.netty.common.netty.SimpleSocksProtocolEncoder;
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;
import org.simplesocks.netty.server.auth.AttributeAuthProvider;
import org.simplesocks.netty.server.config.ServerConfiguration;
import org.simplesocks.netty.server.proxy.ExceptionHandler;
import org.simplesocks.netty.server.proxy.SimpleSocksAuthHandler;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SimpleSocksServer {


	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private Channel serverChannel = null;

	private ServerConfiguration config;

	public SimpleSocksServer(ServerConfiguration config) {
		this.config = config;
	}

	public static void main(String[] args) {
		ServerConfiguration config = new ServerConfiguration();
		config.setPort(11900);
		config.setEnableEpoll(true);
		config.setAuth("123456笑脸☺");
		SimpleSocksServer server = new SimpleSocksServer(config);
		ChannelFuture future = server.start();
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			server.stop();
		}));
		future.channel().close().syncUninterruptibly();
	}


	/**
	 * Main entry
	 */
	public ChannelFuture start() {
		try {

			CompositeEncrypterFactory factory = new CompositeEncrypterFactory();
			factory.registerKey(config.getAuth().getBytes(StandardCharsets.UTF_8));
			AuthProvider authProvider = new AttributeAuthProvider(config.getAuth());

			int idleSecond = config.getChannelTimeoutSeconds();


			ServerBootstrap bootstrap = new ServerBootstrap();

			if(config.isEnableEpoll()){
				log.info("Use epoll on linux, kernel should higher than 2.6.");
				bossGroup = new EpollEventLoopGroup(1);
				workerGroup = new EpollEventLoopGroup();
				bootstrap.channel(EpollServerSocketChannel.class);
			}else{
				bossGroup = new NioEventLoopGroup(1);
				workerGroup = new NioEventLoopGroup();
				bootstrap.channel(NioServerSocketChannel.class);
			}
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64,config.getInitBuffer(), 65536))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = SimpleSocksDecoder.newLengthDecoder();
							socketChannel.pipeline()
									.addLast(new IdleStateHandler(idleSecond, idleSecond, idleSecond, TimeUnit.SECONDS))
									.addLast(lengthFieldBasedFrameDecoder)
									.addLast(new SimpleSocksProtocolDecoder())
									.addLast(new SimpleSocksAuthHandler(authProvider, factory, config))
									.addLast(new ExceptionHandler(authProvider))
									.addFirst(new SimpleSocksProtocolEncoder());
						}
					});

			ChannelFuture channelFuture = bootstrap.bind(config.getPort());
			channelFuture.syncUninterruptibly();
			serverChannel = channelFuture.channel();
			log.info("Simple socks server start at port {}" , config.getPort());
			return channelFuture;
		} catch (Exception e) {
			log.error("Simple socks server start error !", e);
			stop();
			throw new BaseSystemException("Failed to start server "+config);
		}
	}

	/**
	 * close server.
	 */
	public void stop() {
		if(serverChannel!=null)
			serverChannel.close();
        ServerUtils.closeEventLoopGroup(bossGroup);
        ServerUtils.closeEventLoopGroup(workerGroup);
        log.info("Stop Server!");
	}



}
