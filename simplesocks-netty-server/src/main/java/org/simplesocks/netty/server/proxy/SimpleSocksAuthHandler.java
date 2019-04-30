package org.simplesocks.netty.server.proxy;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.protocol.*;
import org.simplesocks.netty.server.auth.AuthProvider;

@Slf4j
public class SimpleSocksAuthHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    private AuthProvider authProvider;

    public SimpleSocksAuthHandler(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksCmdRequest msg) throws Exception {
        DataType type = msg.getType();
        log.debug("receive {} from {}",msg,ctx.channel().remoteAddress());
        switch (type){
            case CONNECT:{
                if(msg instanceof NoAuthConnectionRequest){
                    ctx.channel().writeAndFlush(new ServerResponse(DataType.CONNECT_RESPONSE, ServerResponse.Code.FAIL));
                }else{
                    AuthConnectionRequest request = (AuthConnectionRequest)msg;
                    boolean ok = authProvider.tryAuthenticate(request.getAuth(), ctx.channel().remoteAddress().toString());
                    if(ok){
                        ctx.channel().writeAndFlush(new ServerResponse(DataType.CONNECT_RESPONSE, ServerResponse.Code.SUCCESS));
                    }else{
                        ctx.channel().writeAndFlush(new ServerResponse(DataType.CONNECT_RESPONSE, ServerResponse.Code.FAIL));
                    }
                }
                break;
            }
            default:{
                String identifier = ctx.channel().remoteAddress().toString();
                boolean authenticated = authProvider.authenticated(identifier);
                if(authenticated){
                    ctx.fireChannelRead(msg);
                }else{
                    ctx.channel().writeAndFlush(new ServerResponse(type.toResponse(), ServerResponse.Code.FAIL));
                }
            }
        }
    }




}
