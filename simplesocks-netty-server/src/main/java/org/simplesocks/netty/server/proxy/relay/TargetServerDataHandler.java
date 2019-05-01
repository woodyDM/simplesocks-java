package org.simplesocks.netty.server.proxy.relay;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.simplesocks.netty.common.protocol.ProxyDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOError;
import java.io.IOException;

/**
 *
 * target server data to local server
 * @author
 *
 */
public class TargetServerDataHandler extends ChannelInboundHandlerAdapter {

	private static Logger log = LoggerFactory.getLogger(TargetServerDataHandler.class);
	private Channel toLocalServerChannel;
	RelayProxyDataHandler handler;

	public TargetServerDataHandler(RelayProxyDataHandler handler) {
		this.toLocalServerChannel = handler.getToLocalServerChannel() ;
		this.handler = handler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		handler.onTargetChannelActive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf bytes = (ByteBuf) msg;
		log.debug("write target server data to local server. len {}",bytes.readableBytes());
		try{
			int len = bytes.readableBytes();
			byte[] bytes1 = new byte[len];
			bytes.readBytes(bytes1);
            ProxyDataRequest request = new ProxyDataRequest(bytes1);
            toLocalServerChannel.writeAndFlush(request);
		}finally {
			ReferenceCountUtil.release(bytes);
		}
	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		if(cause instanceof IOException){
			log.warn("io exception, may be channel is forced closed. {}",cause.getMessage());
		}else{
			log.error("exception ", cause);

		}
	}

}
