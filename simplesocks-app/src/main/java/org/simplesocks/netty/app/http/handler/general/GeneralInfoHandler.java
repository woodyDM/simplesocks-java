package org.simplesocks.netty.app.http.handler.general;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;
import org.simplesocks.netty.app.http.handler.general.GeneralInfo;

public class GeneralInfoHandler extends JsonValueHandler {

    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     *
     * @return
     */
    @Override
    public String pathSupport() {
        return "/api/info";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        GeneralInfo info = GeneralInfo.snapshot();
        returnOkJsonContent(AjaxResponse.ok(info), ctx, msg);
    }
}
