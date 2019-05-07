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
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;

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
	private AuthProvider authProvider;
	private Encrypter encrypter = OffsetEncrypter.getInstance();

	public TargetServerDataHandler(Channel toLocalServerChannel, RelayProxyDataHandler handler,AuthProvider authProvider) {
		this.toLocalServerChannel = toLocalServerChannel;
		this.handler = handler;
		this.authProvider = authProvider;
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
            toLocalServerChannel.writeAndFlush(request).addListener(future -> {
            	if(!future.isSuccess()){
            		log.warn("Failed to write to local server channel!");
				}
			});
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
		ServerUtils.logException(log, cause);
		close(ctx);
	}


	private void close(ChannelHandlerContext ctx){
		ctx.channel().close();
		authProvider.remove(toLocalServerChannel);
		if(toLocalServerChannel!=null)
			toLocalServerChannel.close();
	}

}
