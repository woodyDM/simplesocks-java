package org.shadowsocks.netty.server.proxy.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.*;
import org.shadowsocks.netty.common.util.ContentUtils;

@Slf4j
public class RelayProxyDataHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    private ProxyRequest proxyRequest;
    private Bootstrap bootstrap;
    private Channel toTargetServerChannel;

    public RelayProxyDataHandler(ProxyRequest proxyRequest) {
        this.proxyRequest = proxyRequest;
    }

    /**
     * init
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new TargetServerDataHandler(ctx.channel()));
                    }
                });
        bootstrap.connect(proxyRequest.getTarget(), proxyRequest.getPort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            toTargetServerChannel = future.channel();
                            log.info("Success! connect to host {}:{}.", proxyRequest.getTarget(), proxyRequest.getPort());
                            ctx.channel().writeAndFlush(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.SUCCESS));
                        }else{
                            future.channel().close();
                            log.warn("Failed to connect to host {}:{}  , close channel.", proxyRequest.getTarget(), proxyRequest.getPort());
                            ctx.channel().writeAndFlush(new ServerResponse(DataType.PROXY_RESPONSE, ServerResponse.Code.FAIL));
                        }
                    }
                });
    }



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleSocksCmdRequest simpleSocksCmdRequest) throws Exception {
        log.debug("receive data [{}] from localServer",simpleSocksCmdRequest);
        if (simpleSocksCmdRequest instanceof ProxyDataRequest) {
            ProxyDataRequest request = (ProxyDataRequest)simpleSocksCmdRequest;
            if(toTargetServerChannel.isActive()){
                log.info("receive data {} from local server ,proxy for {}",request.getIncomingBuf().readableBytes(), proxyRequest);


                toTargetServerChannel.writeAndFlush(request.getIncomingBuf()).addListener(future -> {
                    if(!future.isSuccess()){
                        log.debug("Failed to proxy data to target server");
                        channelHandlerContext.channel()
                                .writeAndFlush(new ServerResponse(DataType.PROXY_DATA_RESPONSE, ServerResponse.Code.FAIL));
                    }else{
                        log.debug("Success send proxy data to target server. ");
                    }
                });
            }else{
                log.warn("target channel is not active to send data.");
            }
        }else if(simpleSocksCmdRequest instanceof EndProxyRequest){
            if(toTargetServerChannel.isOpen()){
                toTargetServerChannel.close();
            }
            channelHandlerContext.writeAndFlush(new ServerResponse(DataType.END_PROXY_RESPONSE, ServerResponse.Code.SUCCESS))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("error when relay proxy data {}",cause.getMessage());
        ctx.close();
        if(toTargetServerChannel!=null&&toTargetServerChannel.isActive()){
            toTargetServerChannel.close();
        }
    }
}
