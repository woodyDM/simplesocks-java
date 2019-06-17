package org.simplesocks.netty.app.http.handler.git;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;
import org.simplesocks.netty.app.utils.Tuple;

public class GitInfoHandler extends JsonValueHandler {
    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     *
     * @return
     */
    @Override
    public String pathSupport() {
        return "/api/git/info";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        //block but ignore.
        Tuple<String, String> nowProxy = GitRunner.getNowProxy();
        GitInfo info = new GitInfo();
        info.setHttpProxy(nowProxy.getKey());
        info.setHttpsProxy(nowProxy.getValue());
        info.setHasProxyData(nowProxy.getKey()!=null || nowProxy.getValue()!=null);
        AppConfiguration configuration = AppManager.INSTANCE.getConfiguration();
        info.setIsGlobalMode(configuration.isGlobalProxy());
        returnOkJsonContent(AjaxResponse.ok(info), ctx, msg);
    }
}
