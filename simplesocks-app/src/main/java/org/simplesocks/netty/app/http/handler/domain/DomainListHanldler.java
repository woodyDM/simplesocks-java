package org.simplesocks.netty.app.http.handler.domain;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.InvalidRequestHandler;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;

import java.util.List;

public class DomainListHanldler extends JsonValueHandler {
    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     *
     * @return
     */
    @Override
    public String pathSupport() {
        return "/api/domain";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        String type = parseType(msg);
        if(type==null){
            InvalidRequestHandler.INSTANCE.handle(ctx, msg);
            return;
        }
        AppConfiguration config = AppManager.INSTANCE.getConfiguration();
        if(Constants.TYPE_WHITE.equalsIgnoreCase(type)){
            returnOkJsonContent(AjaxResponse.ok(config.getWhiteList()), ctx, msg);
        }else if(Constants.TYPE_PROXY.equalsIgnoreCase(type)){
            returnOkJsonContent(AjaxResponse.ok(config.getProxyList()),ctx, msg);
        }else{
            InvalidRequestHandler.INSTANCE.handle(ctx, msg);
        }
    }


    private String parseType(FullHttpRequest msg){
        QueryStringDecoder de = new QueryStringDecoder(msg.uri());
        List<String> strings = de.parameters().get(Constants.PARAM);
        if(strings==null||strings.isEmpty())
            return null;
        return strings.get(0);
    }


}
