package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.gfw.PacXmlLoader;
import org.simplesocks.netty.app.proxy.relay.direct.DirectRelayClient;
import org.simplesocks.netty.app.utils.ProxyCounter;
import org.simplesocks.netty.common.encrypt.EncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RouterRelayClientManager implements RelayClientManager {

    private ProxyCounter counter;
    private AppConfiguration configuration;
    private RelayClientManager directManager;
    private RelayClientManager simpleSocksManager;
    private Set<String> defaultProxyDomains = new HashSet<>();


    public RouterRelayClientManager(AppConfiguration configuration, ProxyCounter counter, EventLoopGroup loopGroup, EncrypterFactory encrypterFactory) {
        this.counter = counter;
        this.configuration = configuration;
        this.directManager = new DirectRelayClientManager(loopGroup);
        this.simpleSocksManager = new SimpleSocksRelayClientManager(configuration, loopGroup, encrypterFactory);
        Set<String> strings = PacXmlLoader.loadPacSites();
        defaultProxyDomains.addAll(strings);
    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {
        String host = socksCmdRequest.host();
        boolean inWhiteList = isInCollection(configuration.getWhiteList(), host);
        if(inWhiteList){
            return borrowFromDirect(eventExecutor, socksCmdRequest);
        }
        boolean inProxyList = isInCollection(configuration.getProxyList(), host);
        if(inProxyList){
            return borrowFromProxy(eventExecutor, socksCmdRequest);
        }
        boolean inDefaultPacList = isInCollection(defaultProxyDomains, host);
        if(inDefaultPacList){
            return borrowFromProxy(eventExecutor, socksCmdRequest);
        }
        return borrowFromDirect(eventExecutor, socksCmdRequest);

    }

    private <T extends Collection<String>> boolean isInCollection(T c, String host){
        if(c==null||c.isEmpty())
            return false;
        return c.stream().anyMatch(host::contains);
    }

    private Promise<RelayClient> borrowFromDirect(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest){
        return borrowFromManager(directManager, counter.getDirectCounter(), eventExecutor, socksCmdRequest);
    }
    private Promise<RelayClient> borrowFromProxy(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest){
        return borrowFromManager(simpleSocksManager, counter.getProxyCounter(), eventExecutor, socksCmdRequest);
    }

    private Promise<RelayClient> borrowFromManager(RelayClientManager manager, AtomicLong count, EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest){
        Promise<RelayClient> result = eventExecutor.newPromise();
        manager.borrow(eventExecutor, socksCmdRequest).addListener(future -> {
            if(future.isSuccess()){
                result.setSuccess((RelayClient)future.getNow());
                count.incrementAndGet();
            }else{
                if(!(future.cause() instanceof UnknownHostException)){
                    counter.getFailedCounter().incrementAndGet();
                    boolean isDirect = (manager instanceof DirectRelayClientManager);
                    log.warn("Failed to get {} client for [{}], reason is {}", (isDirect?"direct":"proxy"),socksCmdRequest.host(),future.cause().getMessage());
                }
                result.setFailure(future.cause());
            }
        });
        return result;
    }

    @Override
    public void returnClient(RelayClient client) {
        if(client instanceof DirectRelayClient){
            directManager.returnClient(client);
        }else{
            simpleSocksManager.returnClient(client);
        }
    }

}
