package org.simplesocks.netty.server.proxy.relay;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.encrypt.factory.EncrypterFactory;
import org.simplesocks.netty.common.exception.EncInfo;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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



	public TargetServerDataHandler(Channel toLocalServerChannel, RelayProxyDataHandler handler,AuthProvider authProvider,EncrypterFactory encrypterFactory) {
		this.toLocalServerChannel = toLocalServerChannel;
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
			AttributeKey<EncInfo> attributeKey = AttributeKey.valueOf(RelayProxyDataHandler.ENC_ATTRIBUTE_KEY);
			Attribute<EncInfo> attr = toLocalServerChannel.attr(attributeKey);
			EncInfo info = attr.get();
			Encrypter encrypter = encrypterFactory.newInstant(info.getType(), info.getIv());
			int len = bytes.readableBytes();
			byte[] bytes1 = new byte[len];
			bytes.readBytes(bytes1);
			byte[] bytes2 = encrypter.encrypt(bytes1);
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
