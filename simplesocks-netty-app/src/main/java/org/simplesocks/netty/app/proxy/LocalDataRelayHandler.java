package org.simplesocks.netty.app.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.netty.RelayClient;

/**
 * receive data from local app and send to remote server.
 * 
 */
@Slf4j
public class LocalDataRelayHandler extends ChannelInboundHandlerAdapter {

	private RelayClient relayClient;

	public LocalDataRelayHandler(RelayClient relayClient) {
		this.relayClient = relayClient;
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.debug("local app data handler active ready to receive local data {}.",ctx.channel().remoteAddress());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			ByteBuf byteBuf = (ByteBuf)msg;
			int len = byteBuf.readableBytes();
			byte[] bytes = new byte[len];
			byteBuf.readBytes(bytes);
			log.debug("relay local app data to client ,length is [{}].",len);
			relayClient.sendProxyData(bytes);
		}finally {
			ReferenceCountUtil.release(msg);
		}
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		relayClient.close();
		ctx.channel();
		log.error("exception in relay handler ,close channel.", cause);
	}

}
