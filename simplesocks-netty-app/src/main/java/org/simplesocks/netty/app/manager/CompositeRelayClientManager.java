package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.gfw.PacXmlLoader;
import org.simplesocks.netty.app.proxy.relay.direct.DirectRelayClient;
import org.simplesocks.netty.common.encrypt.EncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompositeRelayClientManager implements RelayClientManager {


    private RelayClientManager directManager;
    private RelayClientManager simpleSocksManager;
    private static final int expireMinute = 60;
    private Map<String, LocalDateTime> unableMap = new ConcurrentHashMap<>(256);    //expire time
    private Set<String> forceProxyDomains = new HashSet<>();


    public CompositeRelayClientManager(String host, int port, String auth, EventLoopGroup loopGroup,EncrypterFactory encrypterFactory) {

        this.directManager = new DirectRelayClientManager(loopGroup);
        this.simpleSocksManager = new SimpleSocksRelayClientManager(host, port, auth, loopGroup,encrypterFactory);
        Set<String> strings = PacXmlLoader.loadPacSites();
        forceProxyDomains.addAll(strings);

        forceProxyDomains.add("github.com");
    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {

        String key = getKey(socksCmdRequest);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = unableMap.get(key);
        String host = socksCmdRequest.host();
        boolean needProxy = forceProxyDomains.stream().anyMatch(it -> host.contains(it));
        if(needProxy){
            log.debug("Force proxy!Get SS Proxy client for {}", key);
            return simpleSocksManager.borrow(eventExecutor, socksCmdRequest);
        }
        if(expireTime==null||expireTime.isBefore(now)){ //try direct
            Promise<RelayClient> result = eventExecutor.newPromise();
            directManager.borrow(eventExecutor, socksCmdRequest).addListener(future -> {
                if(future.isSuccess()){
                    log.debug("Get direct client for {}",key);
                    result.setSuccess((RelayClient)future.getNow());
                    unableMap.remove(key);
                }else{
                    unableMap.put(key, now.plusMinutes(expireMinute));
                    log.debug("Try getting SS Proxy client for {}",key);
                    simpleSocksManager.borrow(eventExecutor, socksCmdRequest).addListener(f2->{
                        if(f2.isSuccess()){
                            result.setSuccess((RelayClient)f2.getNow());
                        }else{
                            result.setFailure(new BaseSystemException("Failed to create client for "+key));
                        }
                    });
                }
            });
            return result;
        }else{
            log.debug("Get SS Proxy client for {}",key);
            return simpleSocksManager.borrow(eventExecutor, socksCmdRequest);
        }
    }

    @Override
    public void returnClient(RelayClient client) {
        if(client instanceof DirectRelayClient){
            directManager.returnClient(client);
        }else{
            simpleSocksManager.returnClient(client);
        }
    }

    private String getKey(SocksCmdRequest socksCmdRequest){
        return socksCmdRequest.host();
    }
}
