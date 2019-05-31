package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.simplesocks.netty.app.http.HttpHandler;

import java.nio.charset.StandardCharsets;

public abstract class ContentValueHandler implements HttpHandler {




    protected void returnOkContent(String content, ChannelHandlerContext ctx, FullHttpRequest msg){
        returnContent(content, ctx, HttpResponseStatus.OK, msg);
    }

    protected void returnContent(String content, ChannelHandlerContext ctx, HttpResponseStatus status, FullHttpRequest msg){
        DefaultFullHttpResponse response = generateHttpResponse0(content, status, msg);
        ctx.write(response);
        returnResponse(ctx, HttpUtil.isKeepAlive(msg));
    }

    private DefaultFullHttpResponse generateHttpResponse0(String content, HttpResponseStatus status, FullHttpRequest msg){
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        if(keepAlive)
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        response.content().writeBytes(bytes, 0 , bytes.length);
        return response;
    }

    protected void returnResponse(ChannelHandlerContext ctx, boolean keepAlive){
        ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!keepAlive){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
