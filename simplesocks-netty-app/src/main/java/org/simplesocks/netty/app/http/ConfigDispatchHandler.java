package org.simplesocks.netty.app.http;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.handler.InfoHandler;
import org.simplesocks.netty.app.http.handler.PageNotFoundHandler;
import org.simplesocks.netty.app.http.handler.StaticResourceHandler;

@ChannelHandler.Sharable
@Slf4j
public class ConfigDispatchHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private AppConfiguration configuration;
    private final Dispatcher dispatcher ;


    public ConfigDispatchHandler(AppConfiguration configuration) {
        this.configuration = configuration;
        dispatcher =  new Dispatcher();
        dispatcher.register(InfoHandler.class);
        dispatcher.register(StaticResourceHandler.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String uri = msg.uri();
        String method = msg.method().name();
        HttpHandler httpHandler = dispatcher.get(uri, method);


        httpHandler.handle(ctx, msg, configuration);
    }
}
