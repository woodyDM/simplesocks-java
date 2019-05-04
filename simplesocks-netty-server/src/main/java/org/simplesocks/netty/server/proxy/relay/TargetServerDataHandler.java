package org.simplesocks.netty.server.proxy.relay;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;

import java.io.IOException;

/**
 *
 * target server data to local server
 * @author
 *
 */
@Slf4j
public class TargetServerDataHandler extends ChannelInboundHandlerAdapter {

	private Channel toLocalServerChannel;
	private RelayProxyDataHandler handler;
	private Encrypter encrypter = OffsetEncrypter.getInstance();

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
			byte[] bytes2 = encrypter.encode(bytes1);
			ProxyDataMessage request = new ProxyDataMessage(bytes2);
            toLocalServerChannel.writeAndFlush(request);
		}finally {
			ReferenceCountUtil.release(bytes);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		close(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if(cause instanceof IOException){
			log.warn("io exception, may be channel is forced closed. {}",cause.getMessage());
		}else{
			log.error("exception ", cause);
		}
		close(ctx);
	}


	private void close(ChannelHandlerContext ctx){
		ctx.close();
		if(toLocalServerChannel!=null)
			toLocalServerChannel.close();
	}

}
