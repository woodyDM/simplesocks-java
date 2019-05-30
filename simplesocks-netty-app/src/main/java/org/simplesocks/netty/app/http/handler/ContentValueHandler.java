package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.HttpHandler;

import java.nio.charset.StandardCharsets;

public abstract class ContentValueHandler implements HttpHandler {




    protected DefaultFullHttpResponse generateHttpResponse(String content, FullHttpRequest msg){
        return generateHttpResponse(content,  HttpResponseStatus.OK, msg);
    }

    protected DefaultFullHttpResponse generateHttpResponse(String content, HttpResponseStatus status, FullHttpRequest msg){
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

}
