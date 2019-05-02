package org.simplesocks.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.protocol.*;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;


@Slf4j
public class LocalServerHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {


    private SimpleSocksProtocolClient client;
    private Encrypter encrypter = OffsetEncrypter.getInstance();

    public LocalServerHandler(SimpleSocksProtocolClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(new AuthConnectionRequest(client.getAuth()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksCmdRequest msg) throws Exception {
        log.debug("receive from simpleSocks server: {}",msg);
        DataType type = msg.getType();
        switch (type){
            case CONNECT_RESPONSE:{
                ServerResponse response = (ServerResponse)msg;
                if(response.getCode()== ServerResponse.Code.SUCCESS){
                    client.setConnected(true);
                    client.getConnectionChannelPromise().setSuccess(ctx.channel());
                }else{
                    log.debug("connection auth failed , set promise fail.");
                    client.getConnectionChannelPromise().setFailure(new BaseSystemException("Failed to auth."));
                }
                break;
            }
            case PROXY_RESPONSE: {
                ServerResponse response = (ServerResponse)msg;
                if(response.getCode()== ServerResponse.Code.SUCCESS){
                    client.getProxyChannelPromise().setSuccess(ctx.channel());
                }else{
                    client.getProxyChannelPromise().setFailure(new BaseSystemException("proxy request failed."));
                }
                break;
            }
            case PROXY_DATA:{
                ProxyDataRequest request = (ProxyDataRequest)msg;
                byte[] encoded = request.getBytes();
                byte[] decoded = encrypter.decode(encoded);
                client.onReceiveProxyData(new ProxyDataRequest(decoded));
                break;
            }

            case END_PROXY_RESPONSE:{
                Promise<Void> oldPromise = client.getEndProxyPromise();
                client.clearProxySession();
                oldPromise.setSuccess(null);
                break;
            }
            case END_CONNECTION:{
                log.warn("server request to end connection.");
                ctx.close();
                client.close();
            }
        }
    }




    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException){
            log.warn("exception ,may be force close {}",cause.getMessage());
        }else{
            log.error("exception when communicate with remote server cause is :",cause);
        }
        close(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        close(ctx);
    }

    private void close(ChannelHandlerContext ctx ){
        if(client.getProxyChannelPromise()==null){
            ctx.close();
        }else{
            client.close();
        }
    }

}
