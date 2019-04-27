package org.shadowsocks.netty.client.proxy.relay;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.client.proxy.LocalDataRelayHandler;
import org.shadowsocks.netty.client.proxy.ServerConnectToRemoteHandler;
import org.shadowsocks.netty.client.proxy.SocksServerUtils;
import org.shadowsocks.netty.common.protocol.*;

import javax.security.sasl.AuthenticationException;

@Slf4j
public class RelayHandshakeHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    private RelayClient client;

    public RelayHandshakeHandler(  RelayClient client) {

        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksCmdRequest simpleSocksCmdRequest) throws Exception {
        DataType type = simpleSocksCmdRequest.getType();
        Channel localChannel = ctx.channel();
        Channel outboundChannel = client.getRemoteChannel();
        SocksAddressType socksAddressType = client.getRequest().addressType();
        switch (type){
            case CONNECT_RESPONSE:{
                ServerResponse response =(ServerResponse) simpleSocksCmdRequest;
                log.info("connection {}  ", response);
                if(response.getCode()== ServerResponse.Code.SUCCESS){

                    ProxyRequest.Type proxyType = ProxyRequest.Type.valueOf(socksAddressType.byteValue());

                    outboundChannel.writeAndFlush(new ProxyRequest(proxyType, client.getRequest().port(), client.getRequest().host()))
                            .addListener((proxyFuture->{
                                if(proxyFuture.isSuccess()){
                                    log.info("send proxy request {}:{}  ",client.getRequest().host(), client.getRequest().port());
                                }else{
                                    close(localChannel, ctx, socksAddressType);
                                }
                            }));
                }else{
                    close(ctx.channel(), ctx,socksAddressType);
                    log.error("remote Auth failed , connect failed.");
                }
                break;
            }
            case PROXY_RESPONSE:{
                ServerResponse response =(ServerResponse) simpleSocksCmdRequest;
                log.info("connection {}  ", response);
                if(response.getCode()== ServerResponse.Code.SUCCESS){
                    outboundChannel.pipeline().addLast(new RelayProxyDataHandler(localChannel, outboundChannel, socksAddressType ));
                    localChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, socksAddressType))
                            .addListener((ChannelFutureListener) future->{
                                if(future.isSuccess()){
                                    LocalDataRelayHandler localDataRelay = new LocalDataRelayHandler(client.getRemoteChannel());
                                    localChannel.pipeline().addLast(localDataRelay);
                                }
                            });
                    log.info("proxy established {}",client.getRequest());
                }else{
                    close(ctx.channel(), ctx, socksAddressType);
                    log.error("remote Auth failed , connect failed.");
                }
            }
            default: ctx.fireChannelRead(simpleSocksCmdRequest);
        }
    }



    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("client channel {}, send no auth connection request.", ctx.channel().remoteAddress());
        Channel channel = ctx.channel();
        channel.writeAndFlush(new NoAuthConnectionRequest());
    }

    void close(Channel localChannel, ChannelHandlerContext ctx, SocksAddressType socksAddressType ){
        localChannel.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, socksAddressType));
        SocksServerUtils.closeOnFlush(ctx.channel());
        ctx.close();
    }


}
