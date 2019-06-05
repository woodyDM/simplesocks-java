package org.simplesocks.netty.app;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.ConfigurationServer;
import org.simplesocks.netty.app.proxy.LocalSocksServer;
import org.simplesocks.netty.app.utils.IOExecutor;
import org.simplesocks.netty.app.utils.ProxyCounter;
import org.simplesocks.netty.common.util.ServerUtils;

/**
 * this APP entry
 */
@Slf4j
public class MainApp {

    public static EventLoopGroup bossGroup = null;
    public static EventLoopGroup workerGroup = null;


    public static void main(String[] args) {
        ServerUtils.drawClientStartup(log);
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        Runtime.getRuntime().addShutdownHook(new Thread(()-> stop()));

        AppConfiguration configuration = AppConfiguration.loadOrInit();

        ProxyCounter counter = new ProxyCounter();

        LocalSocksServer proxyServer = LocalSocksServer.newInstance(counter, workerGroup, configuration);
        AppManager.init(configuration, proxyServer, counter, workerGroup);

        proxyServer.start();
        ConfigurationServer configurationServer = new ConfigurationServer(configuration.getConfigServerPort(), workerGroup, configuration);
        configurationServer.start();
    }

    /**
     * destroy this server.
     */
    public static void stop() {
        ServerUtils.closeEventLoopGroup(bossGroup);
        ServerUtils.closeEventLoopGroup(workerGroup);
        ServerUtils.closeEventLoopGroup(IOExecutor.INSTANCE);
        log.info("Release all resources and stop Server!");
    }

}
