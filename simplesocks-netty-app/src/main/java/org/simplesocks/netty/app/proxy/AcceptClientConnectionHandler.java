package org.simplesocks.netty.app.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.util.ServerUtils;

import java.util.concurrent.TimeUnit;

/**
 * SOCK5 hanlder
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
			log.debug("Proxy request {}:{}", req.host(),req.port());
			if (req.cmdType() == SocksCmdType.CONNECT) {
				tryToOpenProxyChannel(ctx, req);
			} else {
				log.error("This server does't not support cmd except CONNECTION, closing ctx: {}",ctx.channel().remoteAddress());
				ctx.close();
			}
			break;
		case UNKNOWN:
			log.error("Unknown cmd[{}], closing ctx: {}",socksRequest.requestType(), ctx.channel().remoteAddress());
			ctx.close();
			break;
		}
	}


	private void tryToOpenProxyChannel(ChannelHandlerContext ctx, SocksCmdRequest socksCmdRequest){
		Channel toLocalChannel = ctx.channel();
		relayClientManager.borrow(ctx.executor(), socksCmdRequest).addListener(future -> {
			if (future.isSuccess()) {
				RelayClient client = (RelayClient)future.getNow();
				log.debug("Get client: {}", client);
				client.onReceiveProxyData(bytes -> {
					if(toLocalChannel.isActive()){
						toLocalChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes)).addListener(f->{
							if(!f.isSuccess()){
								ServerUtils.logException(log,f.cause());
								clear(ctx, client);
							}
						});
					}else{
						log.debug("Local channel is no longer active, trying to close proxy client.");
						clear(ctx, client);
					}
				});
				client.onClose(()->ctx.channel().close());
				client.sendProxyRequest(socksCmdRequest.host(),
						socksCmdRequest.port(),
						ConnectionMessage.Type.valueOf(socksCmdRequest.addressType().byteValue()),
						ctx.executor())
						.addListener(f2 -> {
							if(f2.isSuccess()){ //ready for proxy
								sendSuccessCmd(ctx, toLocalChannel, socksCmdRequest, client);
							}else{
								ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, socksCmdRequest.addressType()))
										.addListener(f4->clear(ctx,client));
								clear(ctx, client);
								log.error("Failed to request proxy for {}:{}, close channel.",socksCmdRequest.host(), socksCmdRequest.port());
							}
						});
			}else{
				log.warn("Failed to get client, check your network or is your remote server running ? Target: {}:{}, close channel!",socksCmdRequest.host(),socksCmdRequest.port());
				ctx.channel().close();
			}
		});
	}

	/**
	 * it is strange sometimes success response send would fail.
	 * so try later instead of closing channel.
	 * @param ctx
	 * @param toLocalChannel
	 * @param socksCmdRequest
	 * @param client
	 */
	private void sendSuccessCmd(ChannelHandlerContext ctx, Channel toLocalChannel, SocksCmdRequest socksCmdRequest, RelayClient client ){
		toLocalChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, socksCmdRequest.addressType()))
				.addListener(f3->{ //when send proxy response ok
					if(f3.isSuccess()){
						ctx.pipeline().remove(AcceptClientConnectionHandler.this);
						ctx.pipeline().addLast(new LocalDataRelayHandler(client));
					}else{
						log.warn("Failed to send connect success back for {}, try later.", socksCmdRequest.host());
						toLocalChannel.eventLoop().schedule(()->{
							if(toLocalChannel!=null && toLocalChannel.isActive()){
								toLocalChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, socksCmdRequest.addressType()))
										.addListener(f4->{
											if(!f4.isSuccess()){
												log.error("Failed to send connect success back for {}, close channel", socksCmdRequest.host());
												clear(ctx, client);
											}else{
												ctx.pipeline().remove(AcceptClientConnectionHandler.this);
												ctx.pipeline().addLast(new LocalDataRelayHandler(client));
											}
										});
							}
						}, 1, TimeUnit.SECONDS);
					}
				});
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		ServerUtils.logException(log, throwable);
		ServerUtils.closeOnFlush(ctx.channel());
	}

	private void clear(ChannelHandlerContext ctx, RelayClient client){
		ctx.channel().close();
		if(client!=null){
			this.relayClientManager.returnClient(client);
		}

	}
}
