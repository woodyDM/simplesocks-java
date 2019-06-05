package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.AppManager;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.AjaxResponse;
import org.simplesocks.netty.app.http.handler.base.RequestBodyHandler;
import org.simplesocks.netty.app.proxy.LocalSocksServer;
import org.simplesocks.netty.app.utils.IOExecutor;
import org.simplesocks.netty.app.utils.ProxyCounter;
@Slf4j
public class SettingHandler extends RequestBodyHandler<AppConfiguration> {

    private AppConfiguration configuration;

    public SettingHandler() {
        this.configuration = AppManager.INSTANCE.getConfiguration();
    }

    @Override
    public String pathSupport() {
        return "/api/setting";
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.POST;
    }

    /**
     *
     *
     *
     * localPort change -> restart server
     * @param body
     * @param ctx
     * @param msg
     */
    @Override
    protected void handle0(AppConfiguration body, ChannelHandlerContext ctx, FullHttpRequest msg) {
        if(configuration.isSame(body)){
            returnOkJsonContent(AjaxResponse.ok(), ctx, msg);
            return;
        }
        try{
            boolean needRestart = (!configuration.getAuth().equals(body.getAuth())) ||
                    configuration.getLocalPort()!=body.getLocalPort();

            configuration.mergeFrom(body);
            IOExecutor.INSTANCE.submit(()->{
                configuration.dump();
            });
            if(needRestart){
                ProxyCounter counter = AppManager.INSTANCE.getCounter();
                LocalSocksServer oldServer = AppManager.INSTANCE.getLocalSocksServer();
                EventLoopGroup eventLoopGroup = AppManager.INSTANCE.getEventLoopGroup();
                oldServer.stop(future -> {
                    LocalSocksServer newServer = LocalSocksServer.newInstance(counter, eventLoopGroup, configuration);
                    AppManager.INSTANCE.setLocalSocksServer(newServer);
                    IOExecutor.INSTANCE.submit(()->{
                        newServer.start().addListener(f->{
                            if(f.isSuccess()){
                                returnOkJsonContent(AjaxResponse.ok(), ctx, msg);
                            }else{
                                log.error("Failed to restart proxy server, {}",f.cause().getMessage());
                                returnOkJsonContent(AjaxResponse.fail("Failed to restart server."), ctx, msg);
                            }
                        });
                    });
                });
            }else{
                returnOkJsonContent(AjaxResponse.ok(), ctx, msg);
            }
        }catch (IllegalArgumentException e){
            InvalidRequestHandler.INSTANCE.handle(ctx, msg);
        }
    }




}
