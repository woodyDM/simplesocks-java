package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;


@Slf4j
public class SettingInfoHandler extends JsonValueHandler {

    private AppConfiguration configuration;

    public SettingInfoHandler() {
        this.configuration = AppManager.INSTANCE.getConfiguration();
    }

    @Override
    public String pathSupport() {
        return "/api/setting";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }


    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg ) {
        AjaxResponse<AppConfiguration> ok = AjaxResponse.ok(configuration);
        returnOkJsonContent(ok, ctx, msg);
    }
}
