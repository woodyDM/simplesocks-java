package org.simplesocks.netty.app.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.util.ServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * 本地浏览器请求远程连接处理handler
 */
public final class ServerConnectToRemoteHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private static Logger logger = LoggerFactory.getLogger(ServerConnectToRemoteHandler.class);

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
                this.client = client;
                logger.debug("Get connected client: {}", client);
                client.setReceiveProxyDataAction(bytes -> {
                    if(toLocalChannel.isActive()){
                        toLocalChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes)).addListener(f->{
                            if(f.isSuccess()){
                                logger.debug("Success write to local, {}",bytes.length);
                            }else{
                                logger.warn("Failed to write to local.",f.cause());
                            }
                        });
                    }else{
                        logger.debug("Local channel is no longer active, trying to close proxy client.");
                        relayClientManager.returnClient(client);
                    }
                });
                client.sendProxyRequest(socksCmdRequest.host(),
                        socksCmdRequest.port(),
                        ConnectionMessage.Type.valueOf(socksCmdRequest.addressType().byteValue()),
                        ctx.executor())
                        .addListener(f2 -> {
                            if(f2.isSuccess()){ //ready for proxy
                                toLocalChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, socksCmdRequest.addressType()))
                                .addListener(f3->{ //when send proxy response ok
                                    try{
                                        ctx.pipeline().remove(this);
                                        ctx.pipeline().addLast(new LocalDataRelayHandler(client));
                                    }catch (NoSuchElementException e){
                                        logger.warn("NoSuchElementException exception ,ignore.");
                                    }catch (Exception e){
                                        logger.error("Exception when proxy ok,",e);
                                    }
                                });
                            }else{
                                ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, socksCmdRequest.addressType()));
                                logger.error("Failed to request proxy for {}:{}",socksCmdRequest.host(), socksCmdRequest.port());
                            }
                        });
            }
        });
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(!(cause instanceof IOException)){
            logger.error("Exception with local channel, close it!",cause);
        }
		this.relayClientManager.returnClient(client);
		this.client = null;
	}

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(client!=null)
            this.relayClientManager.returnClient(client);
    }
}
