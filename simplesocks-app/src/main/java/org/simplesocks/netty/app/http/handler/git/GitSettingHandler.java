package org.simplesocks.netty.app.http.handler.git;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.RequestBodyHandler;
import org.simplesocks.netty.app.utils.IOExecutor;

public class GitSettingHandler extends RequestBodyHandler<GitSettingRequest> {

    @Override
    protected void handle0(GitSettingRequest body, ChannelHandlerContext ctx, FullHttpRequest msg) {
        AppConfiguration configuration = AppManager.INSTANCE.getConfiguration();
        switch (body.getType()){
            case SET:
                run(()->GitRunner.addProxy(configuration.getLocalPort()));
                break;
            case SET_AND_GLOBAL_PROXY:
                run(()->GitRunner.addProxy(configuration.getLocalPort()));
                configuration.setGlobalProxy(true);
                configuration.dumpAsync();
                break;
            case RESET:
                run(()->GitRunner.unsetProxy());
                break;
            case RESET_AND_NO_GLOBAL_PROXY:
                run(()->GitRunner.unsetProxy());
                configuration.setGlobalProxy(false);
                configuration.dumpAsync();
                break;
            default:
                run(()->GitRunner.unsetProxy());
        }
        returnOkJsonContent(AjaxResponse.ok(), ctx, msg);
    }

    /**
     * exact path:   /info (JSON)
     * or start with /static/  -> look for /static at classpath.
     *
     * @return
     */
    @Override
    public String pathSupport() {
        return "/api/git/do";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.POST;
    }

    public void run(Runnable runnable){
        IOExecutor.INSTANCE.submit(runnable);
    }
}
