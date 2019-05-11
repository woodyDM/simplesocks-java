package org.simplesocks.netty.server.proxy.relay;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.factory.EncrypterFactory;
import org.simplesocks.netty.common.encrypt.EncryptInfo;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;

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
	private EncrypterFactory encrypterFactory;



	public TargetServerDataHandler(RelayProxyDataHandler handler,AuthProvider authProvider,EncrypterFactory encrypterFactory) {
		this.toLocalServerChannel = handler.getToLocalServerChannel();
		this.handler = handler;
		this.authProvider = authProvider;
		this.encrypterFactory = encrypterFactory;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		handler.onTargetChannelActive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf bytes = (ByteBuf) msg;
		try{
			EncryptInfo info = handler.getEncryptInfo();
			Encrypter encrypter = encrypterFactory.newInstant(info.getType(), info.getIv());
			int len = bytes.readableBytes();
			byte[] plain = new byte[len];
			bytes.readBytes(plain);
			byte[] encrypt = encrypter.encrypt(plain);
			ProxyDataMessage request = new ProxyDataMessage(encrypt);
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
