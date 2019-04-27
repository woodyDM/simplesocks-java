package org.shadowsocks.netty.client.proxy;

import org.shadowsocks.netty.client.proxy.relay.RelayClient;
import org.shadowsocks.netty.client.proxy.relay.RelayProxyDataHandler;
import org.shadowsocks.netty.common.protocol.ProxyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * 本地浏览器请求远程连接处理handler
 */
public final class ServerConnectToRemoteHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private static Logger logger = LoggerFactory.getLogger(ServerConnectToRemoteHandler.class);

	private RelayClient client ;
	private String remoteIp = "localhost";
	private int remotePort = 10801;
	private SocksCmdRequest request;

	public ServerConnectToRemoteHandler(SocksCmdRequest request) {
		this.request = request;
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SocksCmdRequest socksCmdRequest) throws Exception {
		final Channel inboundChannel = ctx.channel();
		client = new RelayClient(remoteIp, remotePort, inboundChannel.eventLoop() );
		client.setRequest(request);
		client.init();
		ctx.pipeline().remove(this);
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
