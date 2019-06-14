package org.simplesocks.netty.app.http.handler.statistic;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;
import org.simplesocks.netty.app.utils.ProxyCounter;

@Slf4j
public class StatisticHandler extends JsonValueHandler {

    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     *
     * @return
     */
    @Override
    public String pathSupport() {
        return "/api/statistic";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        ProxyCounter counter = AppManager.INSTANCE.getCounter();
        CounterSnapshot snapshot = CounterSnapshot.valueOf(counter);
        returnOkJsonContent(AjaxResponse.ok(snapshot), ctx, msg);
    }
}
