package org.shadowsocks.netty.client.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Promise;
import org.shadowsocks.netty.client.manager.RelayClientManager;
import org.shadowsocks.netty.client.manager.SimpleSocksRelayClientManager;
import org.shadowsocks.netty.common.netty.RelayClient;
import org.shadowsocks.netty.common.protocol.ProxyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本地浏览器请求远程连接处理handler
 */
public final class ServerConnectToRemoteHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private static Logger logger = LoggerFactory.getLogger(ServerConnectToRemoteHandler.class);

	private RelayClientManager relayClientManager = null;
	private RelayClient client ;
	private SocksCmdRequest request;

    public ServerConnectToRemoteHandler(SocksCmdRequest request) {
        this.request = request;
    }


	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		this.relayClientManager = new SimpleSocksRelayClientManager("localhost",10900,"123456笑脸☺",
                ctx.channel().eventLoop(), ctx.executor());
        logger.info("create relayClientManager");

    }


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SocksCmdRequest socksCmdRequest) throws Exception {
        Promise<RelayClient> promise = relayClientManager.borrowerOne().addListener(future -> {
            if (future.isSuccess()) {
                RelayClient client = (RelayClient)future.getNow();
                this.client = client;
                logger.info("get client!");

                client.setReceiveProxyDataAction(bytes -> {
                    ctx.channel().writeAndFlush(bytes);
                });

                client.sendProxyRequest(socksCmdRequest.host(), socksCmdRequest.port(), ProxyRequest.Type.valueOf(socksCmdRequest.addressType().byteValue()))
                        .addListener(f2 -> {
                            if(f2.isSuccess()){
                                ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, socksCmdRequest.addressType()));
                                ctx.pipeline().remove(this);
                                ctx.pipeline().addLast(new LocalDataRelayHandler(client));
                            }else{
                                ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, socksCmdRequest.addressType()));
                                logger.error("failed to request proxy for {}:{}",socksCmdRequest.host(), socksCmdRequest.port());
                            }
                        });
            }
        });
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		SocksServerUtils.closeOnFlush(ctx.channel());
		this.relayClientManager.returnOne(client);
		this.client = null;
	}

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocksServerUtils.closeOnFlush(ctx.channel());
        if(client!=null)
            this.relayClientManager.returnOne(client);
    }
}
