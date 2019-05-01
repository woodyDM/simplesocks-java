package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.proxy.relay.ssocks.SimpleSocksRelayClientAdapter;

import org.simplesocks.netty.client.SimpleSocksProtocolClient;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;

import java.io.IOException;

@Slf4j
public class SimpleSocksRelayClientManager implements RelayClientManager {

    private String host;
    private int port;
    private String auth;
    private EventLoopGroup loopGroup;



    public SimpleSocksRelayClientManager(String host, int port, String auth, EventLoopGroup loopGroup ) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.loopGroup = loopGroup;

    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor) {
        SimpleSocksProtocolClient client = new SimpleSocksProtocolClient(host, port, auth,loopGroup);
        SimpleSocksRelayClientAdapter adapter = new SimpleSocksRelayClientAdapter(client, this);
        Promise<RelayClient> objectPromise = eventExecutor.newPromise();
        client.setConnectionChannelListener(future -> {
            if(future.isSuccess()){
                log.info("get client ok");
                objectPromise.setSuccess(adapter);
            }else{
                log.info("get client failed");
                objectPromise.setFailure(new ProxyConnectException("failed to connect to server."));
            }
        });
        client.init();
        return objectPromise;
    }

    @Override
    public void returnClient(RelayClient client) {
        SimpleSocksRelayClientAdapter adapter = (SimpleSocksRelayClientAdapter)client;
        adapter.getClient().close();
    }
}
