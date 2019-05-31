package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.config.AppConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;

public class PageNotFoundHandler extends FileHandler {

    public static final PageNotFoundHandler INSTANCE = new PageNotFoundHandler();

    @Override
    public String pathSupport() {
        return null;
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, AppConfiguration configuration) {
        try {
            handle(ctx, msg, "/static/404.html");
        } catch (IOException |URISyntaxException e) {
            throw new IllegalStateException("failed to open file 404.html", e);
        }
    }
}
