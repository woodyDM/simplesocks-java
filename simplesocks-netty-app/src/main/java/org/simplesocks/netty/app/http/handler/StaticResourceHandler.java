package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.HttpHandler;

import java.io.File;

public class StaticResourceHandler implements HttpHandler {

    static final String PATH = "/static/";

    @Override
    public String pathSupport() {
        return PATH + "*";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, AppConfiguration configuration) {
        String path = msg.uri();
        if(path.startsWith(PATH)){
            throw new IllegalArgumentException("this handler only handle "+PATH +" uri, the real path is "+path);
        }
        path = path.substring(PATH.length()-1);

    }
}
