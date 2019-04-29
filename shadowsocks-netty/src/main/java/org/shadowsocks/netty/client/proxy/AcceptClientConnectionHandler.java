package org.shadowsocks.netty.client.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;
import lombok.extern.slf4j.Slf4j;

/**
 * SOCK5处理连接请求
 */
@Slf4j
public final class AcceptClientConnectionHandler extends SimpleChannelInboundHandler<SocksRequest> {


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
				log.error("this server does't not support cmd except CONNECTION, closing ctx: {}",ctx.channel().remoteAddress());
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
