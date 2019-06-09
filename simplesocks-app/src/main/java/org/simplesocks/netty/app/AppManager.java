package org.simplesocks.netty.app;


import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.proxy.LocalSocksServer;
import org.simplesocks.netty.app.utils.ProxyCounter;

/**
 * This app SINGLETON instance container.
 */
@Getter
@Setter
public class AppManager {


    private AppConfiguration configuration;
    private LocalSocksServer localSocksServer;
    private ProxyCounter counter;
    private EventLoopGroup eventLoopGroup;

    public static final AppManager INSTANCE = new AppManager();

    public static void init(AppConfiguration configuration, LocalSocksServer localSocksServer, ProxyCounter counter, EventLoopGroup eventLoopGroup){
        INSTANCE.configuration = configuration;
        INSTANCE.localSocksServer = localSocksServer;
        INSTANCE.counter = counter;
        INSTANCE.eventLoopGroup = eventLoopGroup;
    }


}
