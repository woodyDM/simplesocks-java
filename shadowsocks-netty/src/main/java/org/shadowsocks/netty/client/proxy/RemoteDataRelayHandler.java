package org.shadowsocks.netty.client.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 
 * receive data from target server and send to local app
 * 
 */
public final class RemoteDataRelayHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(RemoteDataRelayHandler.class);

	private final Channel relayChannel;
	private ServerConnectToRemoteHandler connectHandler;

	public RemoteDataRelayHandler(Channel relayChannel, ServerConnectToRemoteHandler connectHandler) {
		this.relayChannel = relayChannel;
		this.connectHandler = connectHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			if (relayChannel.isActive()) {
				logger.debug("get remote message" + relayChannel);
				ByteBuf bytebuff = (ByteBuf) msg;
				if (!bytebuff.hasArray()) {
					int len = bytebuff.readableBytes();
					byte[] arr = new byte[len];
					bytebuff.getBytes(0, arr);
					connectHandler.sendLocal(arr, arr.length, relayChannel);
				}
			}
		} catch (Exception e) {
			logger.error("receive remote server data error", e);
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		SocksServerUtils.closeOnFlush(relayChannel);
		SocksServerUtils.closeOnFlush(ctx.channel());
		logger.info("inRelay channelInactive close");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}
}
