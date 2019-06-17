package org.simplesocks.netty.app.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.http.handler.base.FileHandler;

import java.io.FileNotFoundException;

public class IndexHandler extends FileHandler {

    public static final IndexHandler INSTANCE = new IndexHandler();

    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     *
     * @return
     */
    @Override
    public String pathSupport() {
        return "/*";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        try {
            handle(ctx, msg, "/static/index.html");
        } catch (FileNotFoundException e) {
            //
        }
    }
}
