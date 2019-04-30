package org.simplesocks.netty.app.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.manager.RelayClientManager;
import org.simplesocks.netty.app.utils.SocksServerUtils;

/**
 * SOCK5处理连接请求
 */
@Slf4j
public final class AcceptClientConnectionHandler extends SimpleChannelInboundHandler<SocksRequest> {

	private RelayClientManager relayClientManager;

	public AcceptClientConnectionHandler(RelayClientManager relayClientManager) {
		this.relayClientManager = relayClientManager;
	}

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
			SocksCmdRequest req = (SocksCmdRequest) socksRequest;
			if (req.cmdType() == SocksCmdType.CONNECT) {
				ctx.pipeline().addLast(new ServerConnectToRemoteHandler(relayClientManager));
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
