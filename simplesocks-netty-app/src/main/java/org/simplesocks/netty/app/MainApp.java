package org.simplesocks.netty.app;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.ConfigurationServer;
import org.simplesocks.netty.app.manager.RouterRelayClientManager;
import org.simplesocks.netty.app.proxy.LocalSocksServer;
import org.simplesocks.netty.app.utils.ProxyCounter;
import org.simplesocks.netty.common.encrypt.factory.CompositeEncrypterFactory;
import org.simplesocks.netty.common.util.ServerUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * this APP entry
 */
@Slf4j
public class MainApp {

    public static EventLoopGroup bossGroup = null;
    public static EventLoopGroup workerGroup = null;
    private static ExecutorService executor =null;

    public static void main(String[] args) {
        ServerUtils.drawClientStartup(log);
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        executor = Executors.newSingleThreadExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(()-> stop()));

        AppConfiguration configuration = AppConfiguration.load();
        ProxyCounter counter = new ProxyCounter();
        CompositeEncrypterFactory factory = new CompositeEncrypterFactory();
        factory.registerKey(configuration.getAuth().getBytes(StandardCharsets.UTF_8));
        RouterRelayClientManager manager = new RouterRelayClientManager(configuration, counter, workerGroup, factory);
        LocalSocksServer proxyServer = new LocalSocksServer(bossGroup, workerGroup, configuration, manager);
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
        if(executor!=null)
            executor.shutdownNow();
        log.info("Release all resources and stop Server!");
    }

}
