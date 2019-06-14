package org.simplesocks.netty.app;


import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.proxy.LocalSocksServer;
import org.simplesocks.netty.app.utils.ProxyCounter;

import java.time.LocalDateTime;

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
    public static final String VERSION = "v0.0.2";
    public static final LocalDateTime START_TIME = LocalDateTime.now();


    public static final AppManager INSTANCE = new AppManager();

    public static void init(AppConfiguration configuration, LocalSocksServer localSocksServer, ProxyCounter counter, EventLoopGroup eventLoopGroup){
        INSTANCE.configuration = configuration;
        INSTANCE.localSocksServer = localSocksServer;
        INSTANCE.counter = counter;
        INSTANCE.eventLoopGroup = eventLoopGroup;
    }


}
