package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.proxy.relay.direct.DirectRelayClient;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.BaseSystemException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompositeRelayClientManager implements RelayClientManager {

    private String host;
    private int port;
    private String auth;
    private EventLoopGroup loopGroup;
    private RelayClientManager directManager;
    private RelayClientManager simpleSocksManager;
    private static final int expireMinute = 60;
    private Map<String, LocalDateTime> unableMap = new ConcurrentHashMap<>(256);    //expire time


    public CompositeRelayClientManager(String host, int port, String auth, EventLoopGroup loopGroup) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.loopGroup = loopGroup;
        this.directManager = new DirectRelayClientManager(loopGroup);
        this.simpleSocksManager = new SimpleSocksRelayClientManager(host, port, auth, loopGroup);
    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {

        String key = getKey(socksCmdRequest);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = unableMap.get(key);
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
        return socksCmdRequest.host()+"__"+socksCmdRequest.port();
    }
}
