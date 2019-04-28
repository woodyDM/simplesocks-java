package org.shadowsocks.netty.client.proxy;

import org.shadowsocks.netty.common.protocol.ProxyDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * receive data from local app and send to remote server.
 * 
 */
public class LocalDataRelayHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(LocalDataRelayHandler.class);
	private final Channel remoteChannel;

	public LocalDataRelayHandler(Channel remoteChannel) {
		this.remoteChannel = remoteChannel;
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		logger.debug("local app data handler active .{}",ctx.channel().remoteAddress());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			if (remoteChannel.isActive()) {
				ByteBuf bytebuff = (ByteBuf) msg;
				if (!bytebuff.hasArray()) {
					int len = bytebuff.readableBytes();
					byte[] arr = new byte[len];
					bytebuff.getBytes(0, arr);

					ProxyDataRequest proxyData = new ProxyDataRequest(arr);
					remoteChannel.writeAndFlush(proxyData);
				}
			}else{
				logger.debug("remoteChannel is inactive skip.?");
			}
		} catch (Exception e) {
			logger.error("send data to remoteServer error", e);
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		SocksServerUtils.closeOnFlush(remoteChannel);
		SocksServerUtils.closeOnFlush(ctx.channel());
		logger.info("outRelay channelInactive close");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}

}
