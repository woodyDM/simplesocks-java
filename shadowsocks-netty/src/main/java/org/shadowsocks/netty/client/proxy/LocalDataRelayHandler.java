package org.shadowsocks.netty.client.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.netty.RelayClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			log.debug("relay local app data len = {}.",len);
			relayClient.sendProxyData(bytes);
		}finally {
			ReferenceCountUtil.release(msg);
		}
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
		log.error("exception !~", cause);
	}

}
