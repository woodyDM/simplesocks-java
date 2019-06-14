package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;

public class StatisticResetHandler extends JsonValueHandler {
    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     *
     * @return
     */
    @Override
    public String pathSupport() {
        return "/api/statistic/reset";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.POST;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        AppManager.INSTANCE.getCounter().reset();
        returnOkJsonContent(AjaxResponse.ok("重置成功"),ctx,msg);
    }
}
