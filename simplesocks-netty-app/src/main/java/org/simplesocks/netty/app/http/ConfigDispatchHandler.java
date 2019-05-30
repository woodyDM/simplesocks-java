package org.simplesocks.netty.app.http;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.handler.ContentValueHandler;
import org.simplesocks.netty.app.http.handler.InfoHandler;

@ChannelHandler.Sharable
@Slf4j
public class ConfigDispatchHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private AppConfiguration configuration;
    private final Dispatcher dispatcher ;
    private static final HttpHandler NOT_FOUND_HANDLER = new NotFoundHandler();

    public ConfigDispatchHandler(AppConfiguration configuration) {
        this.configuration = configuration;
        dispatcher =  new Dispatcher();
        dispatcher.register(InfoHandler.class);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String uri = msg.uri();
        String method = msg.method().name();
        HttpHandler httpHandler = dispatcher.get(uri, method);
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        if(httpHandler==null){
            httpHandler = NOT_FOUND_HANDLER;
        }
        HttpResponse response = httpHandler.handle(ctx, msg, configuration);
        returnHttpResponse(ctx, response, keepAlive);
    }


    void returnHttpResponse(ChannelHandlerContext ctx, HttpResponse response,boolean keepAlive){
        ctx.write(response);
        ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!keepAlive){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }




    private static class NotFoundHandler extends ContentValueHandler{
        @Override
        public String pathSupport() {
            return null;
        }

        @Override
        public HttpMethod methodSupport() {
            return null;
        }

        @Override
        public HttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest msg, AppConfiguration configuration) {
            return generateHttpResponse("[\"404 not found\"]", HttpResponseStatus.NOT_FOUND, msg);
        }
    }
}
