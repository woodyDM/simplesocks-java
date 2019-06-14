package org.simplesocks.netty.app.http.handler.domain;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.InvalidRequestHandler;
import org.simplesocks.netty.app.http.handler.base.RequestBodyHandler;

import java.util.List;

public class DeleteDomainHanldler extends DomainActionHandler {


    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.DELETE;
    }


    @Override
    protected String handleList(String target, List<String> list, AppConfiguration configuration) {
        boolean removed = list.removeIf(i -> target.equals(i));
        if(removed){
            configuration.dumpAsync();
        }
        return "删除成功";
    }
}
