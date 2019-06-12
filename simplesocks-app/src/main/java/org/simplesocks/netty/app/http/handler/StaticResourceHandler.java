package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.http.Dispatcher;
import org.simplesocks.netty.app.http.handler.base.FileHandler;

import java.io.IOException;

import static org.simplesocks.netty.app.http.Dispatcher.ICON;

/**
 * handler for static resource and index | icon
 */
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
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        String path = msg.uri();
        path = toStatic(path);
        if(!path.startsWith(PATH)){
            throw new IllegalArgumentException("this handler only handle "+PATH +" uri, the real path is "+path);
        }
        try {
            handle(ctx, msg, path);
        } catch (IOException e) {
            PageNotFoundHandler.INSTANCE.handle(ctx, msg);
        }

    }



    private String toStatic(String path){
        final String prefix = "/static";
        if(path.equals(Dispatcher.INDEX))
            return prefix+"/index.html";
        if(path.equalsIgnoreCase(ICON)){
            return prefix + path;
        }
        if(path.startsWith("/umi")){
            return prefix+path;
        }else{
            return path;
        }
    }
}
