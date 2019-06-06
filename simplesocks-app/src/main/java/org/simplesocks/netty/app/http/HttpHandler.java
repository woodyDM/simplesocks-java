package org.simplesocks.netty.app.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public interface HttpHandler {

    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     * @return
     */
    String pathSupport();

    HttpMethod methodSupport();

    void handle(ChannelHandlerContext ctx, FullHttpRequest msg);


}
