package org.simplesocks.netty.app.manager;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.proxy.relay.ssocks.SimpleSocksRelayClientAdapter;
import org.simplesocks.netty.client.SimpleSocksProtocolClient;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.BaseSystemException;

import java.util.*;


@Slf4j
public class SimpleSocksRelayClientPooledManager implements RelayClientManager {

    private String host;
    private int port;
    private String auth;
    private EventLoopGroup loopGroup;
    private final Set<SimpleSocksRelayClientAdapter> pool = new HashSet<>();
    private RelayClientManager manager ;

    public SimpleSocksRelayClientPooledManager(String host, int port, String auth, EventLoopGroup loopGroup ) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.loopGroup = loopGroup;
        this.manager = new SimpleSocksRelayClientManager(host,port,auth,loopGroup);

    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {
        Promise<RelayClient> promise = eventExecutor.newPromise();
        synchronized (pool){
            Iterator<SimpleSocksRelayClientAdapter> iterator = pool.iterator();
            while (iterator.hasNext()){
                SimpleSocksRelayClientAdapter next = iterator.next();
                iterator.remove();
                if(next.getClient().isConnected()){
                    log.debug("get client from pool {}",next.toString());
                    promise.setSuccess(next);
                    return promise;
                }
            }
        }
        return manager.borrow(eventExecutor, socksCmdRequest);
    }

    @Override
    public void returnClient(RelayClient client) {
        SimpleSocksRelayClientAdapter adapter = (SimpleSocksRelayClientAdapter)client;
        SimpleSocksProtocolClient c = adapter.getClient();
        if(c.isConnected()){

            adapter.getClient().endProxy().addListener(future -> {
                if(future.isSuccess()){
                    synchronized (pool){
                        pool.add(adapter);
                        log.debug("valid client, put into pool[size{}]",pool.size());
                    }
                }else{
                    adapter.getClient().forceClose();
                }
            });
        }else{
            adapter.getClient().forceClose();
        }
    }
}
