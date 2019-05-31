package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.HttpHandler;
import org.simplesocks.netty.app.http.handler.base.FileHandler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class StaticResourceHandler extends FileHandler {

    public static final String PATH = "/static/";

    public static final StaticResourceHandler INSTANCE = new StaticResourceHandler();

    /**
     * infact not used
     * @return
     */
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
        if(path.equals("/"))
            path = "/static/index.html";
        if(!path.startsWith(PATH)){
            throw new IllegalArgumentException("this handler only handle "+PATH +" uri, the real path is "+path);
        }
        path = path.substring(PATH.length()-1);
        try {
            handle(ctx, msg, path);
        } catch (IOException e) {
            PageNotFoundHandler.INSTANCE.handle(ctx, msg, configuration);
        }

    }
}
