package org.simplesocks.netty.server.proxy.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.protocol.*;
import org.simplesocks.netty.common.util.ServerUtils;

/**
 * local server data to target server
 */
@Slf4j
public class RelayProxyDataHandler extends SimpleChannelInboundHandler<SimpleSocksMessage> {

    private ConnectionMessage connectionMessage;
    private Channel toLocalServerChannel;
    private EventLoopGroup eventLoopGroup;

    private Channel toTargetServerChannel;
    private Encrypter encrypter = OffsetEncrypter.getInstance();

    public RelayProxyDataHandler(ConnectionMessage connectionMessage) {
        this.connectionMessage = connectionMessage;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.eventLoopGroup = ctx.channel().eventLoop();
        this.toLocalServerChannel = ctx.channel();
    }


    public void tryToConnectToTarget(Channel localChannel){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new TargetServerDataHandler(localChannel,RelayProxyDataHandler.this));
                    }
                });
        bootstrap.connect(connectionMessage.getHost(), connectionMessage.getPort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            toTargetServerChannel = future.channel();
                        }else{
                            log.warn("Failed to connect to target {}:{}.",connectionMessage.getHost(), connectionMessage.getPort());
                            localChannel.writeAndFlush(new ConnectionResponse(ServerResponseMessage.Code.FAIL, connectionMessage.getEncryptType(),"."))
                            .addListener(f2 -> {
                                localChannel.close();
                            });
                        }
                    }
                });
    }


    /**
     *
     */
    public void onTargetChannelActive(){
        String encPassword = "xx";
        ConnectionResponse response = new ConnectionResponse(ServerResponseMessage.Code.SUCCESS, connectionMessage.getEncryptType(), encPassword);
        toLocalServerChannel.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess())
                log.debug("Connect to target {}:{}, encPassword:{}",connectionMessage.getHost(), connectionMessage.getPort(), encPassword );
            else{
                log.error("Failed to send ok response to local server,close all channel!");
                toLocalServerChannel.close();
                toTargetServerChannel.close();
            }
        });
    }

    /**
     * data handle
     * @param channelHandlerContext
     * @param simpleSocksMessage
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleSocksMessage simpleSocksMessage) throws Exception {
        DataType dataType = simpleSocksMessage.getType();
        switch (dataType){

            case PROXY_DATA:{
                ProxyDataMessage request = (ProxyDataMessage) simpleSocksMessage;
                if(!toTargetServerChannel.isActive()){
                    log.warn("target channel is not active, client is too early to send data.");
                    channelHandlerContext.writeAndFlush(new ProxyDataResponse( ServerResponseMessage.Code.FAIL, request.getId()));
                    toTargetServerChannel.close();
                }else{
                    log.debug("receive proxy data {} from local server .", request);
                    byte[] encoded = request.getData();
                    byte[] decoded = encrypter.decode(encoded);
                    toTargetServerChannel.writeAndFlush(Unpooled.wrappedBuffer(decoded)).addListener(future -> {
                        if(!future.isSuccess()){
                            log.warn("Failed to proxy data to target server,close all channel.");
                            channelHandlerContext.channel()
                                    .writeAndFlush(new ProxyDataResponse(ServerResponseMessage.Code.FAIL, request.getId()))
                            .addListener(f1->clear(channelHandlerContext));
                        }else{
                            channelHandlerContext.channel()
                                    .writeAndFlush(new ProxyDataResponse(ServerResponseMessage.Code.SUCCESS, request.getId()));
                        }
                    });
                }
                break;
            }
            default:
                throw new IllegalStateException("impossible to here");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clear(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ServerUtils.logException(log, cause);
        clear(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            clear(ctx).addListener(future -> {
                log.warn("Too long to interactive with remote channel. event={}[{}]", event.state(),future.isSuccess());
            });
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }


    private ChannelFuture clear(ChannelHandlerContext ctx){
        if(toTargetServerChannel!=null)
            toTargetServerChannel.close();      //close channel to target server.
        return ctx.channel().close();  //close channel to local server.
    }
}
