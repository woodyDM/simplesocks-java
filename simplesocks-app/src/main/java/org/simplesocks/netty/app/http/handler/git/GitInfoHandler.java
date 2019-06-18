package org.simplesocks.netty.app.http.handler.git;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.concurrent.Future;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.JsonValueHandler;
import org.simplesocks.netty.app.utils.IOExecutor;
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
        Future<Tuple<String, String>> task = IOExecutor.INSTANCE.submit(() -> GitRunner.getNowProxy());
        task.addListener(f->{
           if(f.isSuccess()){
               Tuple<String, String> nowProxy = task.getNow();
               GitInfo info = new GitInfo();
               info.setHttpProxy(nowProxy.getKey());
               info.setHttpsProxy(nowProxy.getValue());
               info.setHasProxyData(nowProxy.getKey()!=null || nowProxy.getValue()!=null);
               AppConfiguration configuration = AppManager.INSTANCE.getConfiguration();
               info.setIsGlobalMode(configuration.isGlobalProxy());
               info.setLocalPort(configuration.getLocalPort());
               returnOkJsonContent(AjaxResponse.ok(info), ctx, msg);
           }else{
               returnOkJsonContent(AjaxResponse.fail("Git信息获取失败"), ctx, msg);
           }
        });
    }
}
