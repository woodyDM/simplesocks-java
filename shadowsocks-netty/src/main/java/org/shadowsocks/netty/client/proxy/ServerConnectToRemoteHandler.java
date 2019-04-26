package org.shadowsocks.netty.client.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.util.NoSuchElementException;

/**
 * 本地浏览器请求远程连接处理handler
 */
public final class ServerConnectToRemoteHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private static Logger logger = LoggerFactory.getLogger(ServerConnectToRemoteHandler.class);
	private final Bootstrap b = new Bootstrap();


	private String remoteIp = "127.0.0.1";
	private int remotePort = 10900;

	/**
	 * read
	 * @param ctx
	 * @param request
	 * @throws Exception
	 */
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest request) throws Exception {
		String targetHost = request.host();
		int targetPort = request.port();

		Promise<Channel> promise = ctx.executor().newPromise();
		promise.addListener(new GenericFutureListener<Future<Channel>>() {
			@Override
			public void operationComplete(final Future<Channel> future) throws Exception {
				final Channel outboundChannel = future.getNow();
				if (future.isSuccess()) {
					/**
					 *           ctx.channel()               future.getNow()
 					 *  Broswer ---------------> ThisServer -----------------> TargetServer
					 *               IN                           OUT
					 */
					RemoteDataRelayHandler inRelay = new RemoteDataRelayHandler(ctx.channel(), ServerConnectToRemoteHandler.this);
					LocalDataRelayHandler outRelay = new LocalDataRelayHandler(outboundChannel, ServerConnectToRemoteHandler.this);
					/**
					 * set socks5 success signal and remove this handler,
					 * then the local app will send bytes , which will handle by LocalDataRelayHandler.
					 * Add handler for out pipeline.
					 */
					ctx.channel().writeAndFlush(getSuccessResponse(request)).addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture channelFuture) {
							try {
								outboundChannel.pipeline().addLast(inRelay);
								ctx.pipeline().addLast(outRelay);
								ctx.pipeline().remove(ServerConnectToRemoteHandler.this);
							} catch (NoSuchElementException e) {
								//ignore	?? why
							} catch (Exception e){
								logger.error("", e);
							}
						}
					});
				} else {
					ctx.channel().writeAndFlush(getFailureResponse(request));
					SocksServerUtils.closeOnFlush(ctx.channel());
					logger.warn("@@@Failed to connect to host {} port {} proxy {}, close channel." , request.host() , request.port() );
				}
			}
		});


		final Channel inboundChannel = ctx.channel();
		b.group(inboundChannel.eventLoop())
				.channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.handler(new RemoteConnectedHandler(promise));
		b.connect(targetHost, targetPort)
				.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					ctx.channel().writeAndFlush(getFailureResponse(request));
					SocksServerUtils.closeOnFlush(ctx.channel());
					logger.warn("Failed to connect to host {} port {}  , close channel." , request.host() , request.port() );
				}else{
					//only connect success
					//the success response to local socks5 connection is send by
					//promise above
					logger.info("Success Connect to host {} port {}  " , request.host() , request.port() );
				}
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		SocksServerUtils.closeOnFlush(ctx.channel());
	}







	private SocksCmdResponse getSuccessResponse(SocksCmdRequest request) {
		return new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4);
	}

	private SocksCmdResponse getFailureResponse(SocksCmdRequest request) {
		return new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4);
	}

	/**
	 * localserver和remoteserver进行connect发送的数据
	 * 
	 * @param request
	 * @param outboundChannel
	 */
	private void sendConnectRemoteMessage(SocksCmdRequest request, Channel outboundChannel) {
		ByteBuf buff = Unpooled.buffer();
		request.encodeAsByteBuf(buff);
		if (buff.hasArray()) {
			int len = buff.readableBytes();
			byte[] arr = new byte[len];
			buff.getBytes(0, arr);
			byte[] data = remoteByte(arr);
			sendRemote(data, data.length, outboundChannel);
		}
	}

	/**
	 * localserver和remoteserver进行connect发送的数据
	 * 
	 * +-----+-----+-------+------+----------+----------+ | VER | CMD | RSV |
	 * ATYP | DST.ADDR | DST.PORT |
	 * +-----+-----+-------+------+----------+----------+ | 1 | 1 | X'00' | 1 |
	 * Variable | 2 | +-----+-----+-------+------+----------+----------+
	 * 
	 * 需要跳过前面3个字节
	 * 
	 * @param data
	 * @return
	 */
	private byte[] remoteByte(byte[] data) {
		int dataLength = data.length;
		dataLength -= 3;
		byte[] temp = new byte[dataLength];
		System.arraycopy(data, 3, temp, 0, dataLength);
		return temp;
	}

	/**
	 * 给remoteserver发送数据--需要进行加密处理
	 * 
	 * @param data
	 * @param length
	 * @param channel
	 */
	public void sendRemote(byte[] data, int length, Channel channel) {
		channel.writeAndFlush(Unpooled.wrappedBuffer(data));
		logger.debug("sendRemote message:isProxy = " + false + ",length = " + length + ",channel = " + channel);
	}

	/**
	 * 给本地客户端回复消息--需要进行解密处理
	 * 
	 * @param data
	 * @param length
	 * @param channel
	 */
	public void sendLocal(byte[] data, int length, Channel channel) {
		channel.writeAndFlush(Unpooled.wrappedBuffer(data));
		logger.debug("sendLocal message:isProxy = " + false + ",length = " + length + ",channel = " + channel);
	}

}
