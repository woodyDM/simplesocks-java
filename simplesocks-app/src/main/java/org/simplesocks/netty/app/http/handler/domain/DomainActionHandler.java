package org.simplesocks.netty.app.http.handler.domain;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.InvalidRequestHandler;
import org.simplesocks.netty.app.http.handler.base.RequestBodyHandler;

import java.util.List;

public abstract class DomainActionHandler extends RequestBodyHandler<DomainRequest> {

    @Override
    protected void handle0(DomainRequest body, ChannelHandlerContext ctx, FullHttpRequest msg) {
        if(body==null||!body.isValid()){
            InvalidRequestHandler.INSTANCE.handle(ctx, msg);
            return;
        }
        AppConfiguration config = AppManager.INSTANCE.getConfiguration();
        List<String> domains = body.getType().equals(Constants.TYPE_WHITE) ? config.getWhiteList():config.getProxyList();
        String message = handleList(body.getDomain(), domains, config);
        returnOkJsonContent(AjaxResponse.ok(message), ctx ,msg);
    }


    protected abstract String handleList(String target, List<String> list, AppConfiguration configuration);

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
}
