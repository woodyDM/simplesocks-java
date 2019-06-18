package org.simplesocks.netty.app.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.http.handler.*;
import org.simplesocks.netty.app.http.handler.domain.AddDomainHanldler;
import org.simplesocks.netty.app.http.handler.domain.DeleteDomainHanldler;
import org.simplesocks.netty.app.http.handler.domain.DomainListHanldler;
import org.simplesocks.netty.app.http.handler.general.GeneralInfoHandler;
import org.simplesocks.netty.app.http.handler.git.GitInfoHandler;
import org.simplesocks.netty.app.http.handler.git.GitSettingHandler;
import org.simplesocks.netty.app.http.handler.setting.SettingHandler;
import org.simplesocks.netty.app.http.handler.setting.SettingInfoHandler;
import org.simplesocks.netty.app.http.handler.statistic.StatisticHandler;
import org.simplesocks.netty.app.http.handler.statistic.StatisticResetHandler;

@ChannelHandler.Sharable
@Slf4j
public class ConfigDispatchHandler extends SimpleChannelInboundHandler<FullHttpRequest> {



    private final static Dispatcher dispatcher ;
    static {

        dispatcher = new Dispatcher();
        dispatcher.register("/api/setting", "GET", new SettingInfoHandler());
        dispatcher.register("/api/setting", "POST", new SettingHandler());
        dispatcher.register("/api/statistic", "GET", new StatisticHandler());
        dispatcher.register("/api/statistic/reset", "POST", new StatisticResetHandler());
        dispatcher.register("/api/domain", "GET", new DomainListHanldler());
        dispatcher.register("/api/domain", "POST", new AddDomainHanldler());
        dispatcher.register("/api/domain/delete", "POST", new DeleteDomainHanldler());
        dispatcher.register("/api/git/info", "GET", new GitInfoHandler());
        dispatcher.register("/api/git/do", "POST", new GitSettingHandler());

        dispatcher.register("/api/info", "GET", new GeneralInfoHandler());
        dispatcher.register("/static/*","GET", new StaticResourceHandler());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String uri = msg.uri();
        //handle GET method
        int index = uri.indexOf("?");
        if(index!=-1){
            uri = uri.substring(0, index);
        }
        String method = msg.method().name();
        HttpHandler httpHandler = dispatcher.get(uri, method);
        httpHandler.handle(ctx, msg);
    }
}
