package org.simplesocks.netty.server.proxy.relay;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.factory.EncrypterFactory;
import org.simplesocks.netty.common.encrypt.EncryptInfo;
import org.simplesocks.netty.common.protocol.*;
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;
import org.simplesocks.netty.server.config.ServerConfiguration;

import java.util.Arrays;

/**
 * local server data to target server
 */
@Slf4j
@Getter
public class RelayProxyDataHandler extends SimpleChannelInboundHandler<SimpleSocksMessage> {

    private ConnectionMessage connectionMessage;
    private AuthProvider authProvider;
    private EncrypterFactory encrypterFactory;
    private ServerConfiguration configuration;

    private EventLoopGroup eventLoopGroup;
    private Channel toLocalServerChannel;

    private Channel toTargetServerChannel;
    private EncryptInfo encryptInfo;


    public RelayProxyDataHandler(ConnectionMessage connectionMessage, AuthProvider authProvider,EncrypterFactory encrypterFactory, ServerConfiguration configuration ) {
        this.connectionMessage = connectionMessage;
        this.authProvider = authProvider;
        this.encrypterFactory = encrypterFactory;
        this.configuration = configuration;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.eventLoopGroup = ctx.channel().eventLoop();
        this.toLocalServerChannel = ctx.channel();
    }


    public void tryToConnectToTarget(){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        if(configuration.isEnableEpoll()){
            bootstrap.channel(EpollSocketChannel.class);
        }else{
            bootstrap.channel(NioSocketChannel.class);
        }
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64,configuration.getInitBuffer(), 65536))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new TargetServerDataHandler(RelayProxyDataHandler.this, authProvider,encrypterFactory));

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
                            toLocalServerChannel.writeAndFlush(ConnectionResponse.fail( connectionMessage.getEncryptType() ))
                            .addListener(f2 -> {
                                toLocalServerChannel.close();
                            });
                        }
                    }
                });
    }


    /**
     * when target channel active ,send ok response to local channel
     */
    public void onTargetChannelActive(){
        String encryptType = connectionMessage.getEncryptType();
        byte[] iv = encrypterFactory.randomIv(encryptType);
        encryptInfo = new EncryptInfo(encryptType, iv);
        ConnectionResponse response = new ConnectionResponse(ServerResponseMessage.Code.SUCCESS, connectionMessage.getEncryptType(), iv);
        toLocalServerChannel.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess())
                log.debug("To target {}:{}, channel {};iv {}",connectionMessage.getHost(), connectionMessage.getPort(), toLocalServerChannel.remoteAddress(), Arrays.toString(iv) );
            else{
                log.error("Failed to send ok response to local server,close all channel!");
                toLocalServerChannel.close();
                toTargetServerChannel.close();
            }
        });
    }

    /**
     * data handle
     * @param ctx
     * @param simpleSocksMessage
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksMessage simpleSocksMessage) throws Exception {
        DataType dataType = simpleSocksMessage.getType();
        switch (dataType){

            case PROXY_DATA:{
                ProxyDataMessage request = (ProxyDataMessage) simpleSocksMessage;
                if(!toTargetServerChannel.isActive()){
                    log.warn("target channel is not active, client is too early to send data.");
                    ctx.writeAndFlush(new ProxyDataResponse(ServerResponseMessage.Code.FAIL, request.getId()));
                    clear(ctx);
                }else{
                    byte[] encrypt = request.getData();
                    Encrypter encrypter = encrypterFactory.newInstant(encryptInfo.getType(), encryptInfo.getIv());
                    byte[] plain = encrypter.decrypt(encrypt);
                    toTargetServerChannel.writeAndFlush(Unpooled.wrappedBuffer(plain)).addListener(future -> {
                        if(!future.isSuccess()){
                            log.warn("Failed to proxy data to target server {}, close all channel.", toTargetServerChannel.remoteAddress());
                            ctx.channel()
                                    .writeAndFlush(new ProxyDataResponse(ServerResponseMessage.Code.FAIL, request.getId()))
                            .addListener(f1->clear(ctx));
                        }else{
                            ctx.channel()
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
        authProvider.remove(ctx.channel());
        if(toTargetServerChannel!=null)
            toTargetServerChannel.close();      //close channel to target server.
        return ctx.channel().close();           //close channel to local server.
    }
}
