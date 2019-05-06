package org.simplesocks.netty.app.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.util.ServerUtils;

import java.io.IOException;

/**
 * 本地浏览器请求远程连接处理handler
 */
@Slf4j
public final class ServerConnectToRemoteHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {


	private RelayClientManager relayClientManager ;
    private RelayClient client ;


    public ServerConnectToRemoteHandler(RelayClientManager relayClientManager) {
        this.relayClientManager = relayClientManager;
    }


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SocksCmdRequest socksCmdRequest) throws Exception {
        Channel toLocalChannel = ctx.channel();
        relayClientManager.borrow(ctx.executor(), socksCmdRequest).addListener(future -> {
            if (future.isSuccess()) {
                RelayClient client = (RelayClient)future.getNow();
                ServerConnectToRemoteHandler.this.client = client;
                log.debug("Get client: {}", client);
                client.onReceiveProxyData(bytes -> {
                    if(toLocalChannel.isActive()){
                        toLocalChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes)).addListener(f->{
                            if(f.isSuccess()){
                                log.debug("Success write to local, {}",bytes.length);
                            }else{
                                ServerUtils.logException(log,f.cause());
                                clear(ctx);
                            }
                        });
                    }else{
                        log.debug("Local channel is no longer active, trying to close proxy client.");
                        clear(ctx);
                    }
                });
                client.onClose(()->ctx.channel().close());
                client.sendProxyRequest(socksCmdRequest.host(),
                        socksCmdRequest.port(),
                        ConnectionMessage.Type.valueOf(socksCmdRequest.addressType().byteValue()),
                        ctx.executor())
                        .addListener(f2 -> {
                            if(f2.isSuccess()){ //ready for proxy
                                toLocalChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, socksCmdRequest.addressType()))
                                .addListener(f3->{ //when send proxy response ok
                                    if(f3.isSuccess()){
                                        ctx.pipeline().remove(ServerConnectToRemoteHandler.this);
                                        ctx.pipeline().addLast(new LocalDataRelayHandler(client));
                                    }else{
                                        log.warn("failed to send connect success back, close channel.{}",ctx.channel().remoteAddress());
                                        clear(ctx);
                                    }
                                });
                            }else{
                                ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, socksCmdRequest.addressType()))
                                .addListener(f4->clear(ctx));
                                log.error("Failed to request proxy for {}:{}, close channel.",socksCmdRequest.host(), socksCmdRequest.port());
                            }
                        });
            }else{
                log.warn("Failed to get relay client for {}:{}, close channel!",socksCmdRequest.host(),socksCmdRequest.port());
                ctx.channel().close();
            }
        });
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(!(cause instanceof IOException)){
            log.error("Exception with local channel, close it!",cause);
        }else{
            log.error("IOException, {}",cause.getMessage());
        }
		clear(ctx);
	}



    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clear(ctx);
    }


    private void clear(ChannelHandlerContext ctx){
        ctx.channel().close();
        if(client!=null){
            this.relayClientManager.returnClient(client);
            client=null;
        }

    }
}
