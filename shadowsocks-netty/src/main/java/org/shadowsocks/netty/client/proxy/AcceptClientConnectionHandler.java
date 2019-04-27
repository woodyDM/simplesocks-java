package org.shadowsocks.netty.client.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAuthResponse;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksAuthStatus;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdRequestDecoder;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksInitResponse;
import io.netty.handler.codec.socks.SocksRequest;

/**
 * SOCK5处理连接请求
 */
public final class AcceptClientConnectionHandler extends SimpleChannelInboundHandler<SocksRequest> {

	private static Logger logger = LoggerFactory.getLogger(AcceptClientConnectionHandler.class);

	@Override
	public void channelRead0(ChannelHandlerContext ctx, SocksRequest socksRequest) throws Exception {
		switch (socksRequest.requestType()) {
		case INIT: {
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.write(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
			break;
		}
		case AUTH:
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
			break;
		case CMD:
			//浏览器请求连接
			SocksCmdRequest req = (SocksCmdRequest) socksRequest;
			if (req.cmdType() == SocksCmdType.CONNECT) {
				ctx.pipeline().addLast(new ServerConnectToRemoteHandler(req));
				ctx.pipeline().remove(this);
				ctx.fireChannelRead(socksRequest);
			} else {
				ctx.close();
			}
			break;
		case UNKNOWN:
			ctx.close();
			break;
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		SocksServerUtils.closeOnFlush(ctx.channel());
	}
}
