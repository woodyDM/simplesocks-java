package org.shadowsocks.netty.client.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.client.proxy.relay.SimpleSocksRelayClientAdapter;
import org.shadowsocks.netty.common.netty.RelayClient;
import org.shadowsocks.netty.server.SimpleSocksProtocolClient;

import java.io.IOException;

@Slf4j
public class SimpleSocksRelayClientManager implements RelayClientManager {

    private String host;
    private int port;
    private String auth;
    private EventLoopGroup loopGroup;
    private EventExecutor eventExecutor;


    public SimpleSocksRelayClientManager(String host, int port, String auth, EventLoopGroup loopGroup, EventExecutor eventExecutor) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.loopGroup = loopGroup;
        this.eventExecutor = eventExecutor;
    }

    @Override
    public Promise<RelayClient> borrowerOne() {
        SimpleSocksProtocolClient client = new SimpleSocksProtocolClient(host, port, auth,loopGroup);
        SimpleSocksRelayClientAdapter adapter = new SimpleSocksRelayClientAdapter(client);
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
    public void returnOne(RelayClient client) {
        SimpleSocksRelayClientAdapter adapter = (SimpleSocksRelayClientAdapter)client;
        try {
            adapter.getClient().close();
        } catch (IOException e) {
            //ignore;
        }
    }
}
