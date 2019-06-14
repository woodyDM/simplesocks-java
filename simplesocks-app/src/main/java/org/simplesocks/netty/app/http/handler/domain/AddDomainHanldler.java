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

public class AddDomainHanldler extends DomainActionHandler {
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
        return HttpMethod.POST;
    }

    @Override
    protected String handleList(String target, List<String> domains, AppConfiguration configuration) {
        if(!domains.contains(target)){
            domains.add(target);
            configuration.dumpAsync();
        }
        return "添加成功";
    }


}
