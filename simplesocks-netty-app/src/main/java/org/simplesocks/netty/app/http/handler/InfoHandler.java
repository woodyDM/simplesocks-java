package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.JsonUtils;
import org.simplesocks.netty.app.http.handler.base.ContentValueHandler;


@Slf4j
public class InfoHandler extends ContentValueHandler {

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
        String json = JsonUtils.toJson(ok);
        returnOkContent(Constants.APPLICATION_JSON, json, ctx, msg);
    }
}
