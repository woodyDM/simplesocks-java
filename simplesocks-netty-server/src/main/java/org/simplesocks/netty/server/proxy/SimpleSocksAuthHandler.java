package org.simplesocks.netty.server.proxy;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.protocol.*;
import org.simplesocks.netty.server.auth.AuthProvider;
import org.simplesocks.netty.server.proxy.relay.RelayProxyDataHandler;

@Slf4j
public class SimpleSocksAuthHandler extends SimpleChannelInboundHandler<SimpleSocksMessage> {

    private AuthProvider authProvider;  //TODO modify to attributekey

    public SimpleSocksAuthHandler(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksMessage msg) throws Exception {
        DataType type = msg.getType();
        log.debug("receive {} from {}",msg,ctx.channel().remoteAddress());
        switch (type){
            case CONNECT:{
                ConnectionMessage request = (ConnectionMessage)msg;
                boolean ok = authProvider.tryAuthenticate(request.getAuth(), ctx.channel().remoteAddress().toString());
                if(ok){
                    RelayProxyDataHandler relayProxyDataHandler = new RelayProxyDataHandler(request);
                    ctx.pipeline().addLast(relayProxyDataHandler);
                    relayProxyDataHandler.tryToConnectToTarget(ctx.channel());
                }else{
                    ctx.channel().writeAndFlush(new ConnectionResponse(ServerResponseMessage.Code.FAIL, request.getEncryptType(),"."));
                }
                break;
            }
            case PROXY_DATA:{
                String identifier = ctx.channel().remoteAddress().toString();
                boolean authenticated = authProvider.authenticated(identifier);
                if(authenticated){
                    ctx.fireChannelRead(msg);
                }else{
                    ProxyDataMessage request = (ProxyDataMessage)msg;
                    ctx.channel().writeAndFlush(new ProxyDataResponse(ServerResponseMessage.Code.FAIL, request.getId()));
                }
                break;
            }
            default:
                throw new IllegalStateException("client send data illegal! "+type);
        }
    }

}
