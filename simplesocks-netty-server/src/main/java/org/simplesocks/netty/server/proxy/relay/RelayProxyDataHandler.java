package org.simplesocks.netty.server.proxy.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.protocol.*;

/**
 * local server data to target server
 */
@Slf4j
public class RelayProxyDataHandler extends SimpleChannelInboundHandler<SimpleSocksMessage> {

    private ConnectionMessage connectionMessage;
    private Bootstrap bootstrap;
    private Channel toTargetServerChannel;
    private Channel toLocalServerChannel;
    private EventLoopGroup eventLoopGroup;
    private Encrypter encrypter = OffsetEncrypter.getInstance();


    public RelayProxyDataHandler(ConnectionMessage connectionMessage, Channel channel) {
        this.connectionMessage = connectionMessage;
        this.toLocalServerChannel = channel;

    }

    public void tryToConnectToTarget(){
        Channel localChannel = toLocalServerChannel;
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new TargetServerDataHandler(RelayProxyDataHandler.this));
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
                            localChannel.writeAndFlush(new ConnectionResponse(ServerResponseMessage.Code.FAIL, connectionMessage.getEncryptType(),"."));
                            toLocalServerChannel.close();
                        }
                    }
                });
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.eventLoopGroup = ctx.channel().eventLoop();
    }


    public Channel getToLocalServerChannel() {
        return toLocalServerChannel;
    }


    /**
     *
     */
    public void onTargetChannelActive(){
        String encPassword = "xx";
        ConnectionResponse response = new ConnectionResponse(ServerResponseMessage.Code.SUCCESS, connectionMessage.getEncryptType(), encPassword);
        toLocalServerChannel.writeAndFlush(response).addListener(future1 -> {
            if(future1.isSuccess())
                log.debug("Connect to target {}:{}, send response {}",connectionMessage.getHost(), connectionMessage.getPort(), encPassword );
            else
                log.error("Failed to send ok response to local server!");
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
                }else{
                    log.info("receive proxy data {} from local server .", request);
                    byte[] encoded = request.getData();
                    byte[] decoded = encrypter.decode(encoded);
                    ProxyDataMessage requestT = new ProxyDataMessage(encoded);
                    toTargetServerChannel.writeAndFlush(Unpooled.wrappedBuffer(decoded)).addListener(future -> {
                        if(!future.isSuccess()){
                            log.debug("Failed to proxy data to target server");
                            channelHandlerContext.channel()
                                    .writeAndFlush(new ProxyDataResponse( ServerResponseMessage.Code.FAIL, request.getId()));
                        }else{
                            channelHandlerContext.channel()
                                    .writeAndFlush(new ProxyDataResponse( ServerResponseMessage.Code.SUCCESS, request.getId()));
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
        close(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("error when relay proxy data {}",cause.getMessage());
        close(ctx);
    }


    private void close(ChannelHandlerContext ctx){
        ctx.close();
        if(toTargetServerChannel!=null){
            toTargetServerChannel.close();
        }
    }

}
