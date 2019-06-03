package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;


@Slf4j
public class InfoHandler extends JsonValueHandler {

    @Override
    public String pathSupport() {
        return "/info";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }


    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, AppConfiguration configuration) {
        AjaxResponse<AppConfiguration> ok = AjaxResponse.ok(configuration);
        returnOkJsonContent(ok, ctx, msg);
    }
}
