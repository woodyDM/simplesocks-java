package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.proxy.relay.ssocks.SimpleSocksRelayClientAdapter;
import org.simplesocks.netty.client.SimpleSocksProtocolClient;
import org.simplesocks.netty.common.encrypt.EncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;


@Slf4j
public class SimpleSocksRelayClientManager implements RelayClientManager {

    private AppConfiguration configuration;
    private EventLoopGroup loopGroup;
    private EncrypterFactory encrypterFactory;


    public SimpleSocksRelayClientManager(AppConfiguration configuration, EventLoopGroup loopGroup, EncrypterFactory encrypterFactory) {
        this.configuration = configuration;
        this.loopGroup = loopGroup;
        this.encrypterFactory = encrypterFactory;
    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {
        SimpleSocksProtocolClient client = new SimpleSocksProtocolClient(configuration.getAuth(), configuration.getEncryptType(), configuration.getRemoteHost(), configuration.getRemotePort(), loopGroup, encrypterFactory);
        SimpleSocksRelayClientAdapter adapter = new SimpleSocksRelayClientAdapter(client);
        Promise<RelayClient> promise = eventExecutor.newPromise();
        client.init().addListener(future -> {
            if(future.isSuccess()){
                promise.setSuccess(adapter);
            }else{
                promise.setFailure(new BaseSystemException("Failed connect to server "+configuration.getRemoteHost()+"with auth "+configuration.getAuth()));
            }
        }) ;
        return promise;
    }

    @Override
    public void returnClient(RelayClient client) {
        SimpleSocksRelayClientAdapter adapter = (SimpleSocksRelayClientAdapter)client;
        adapter.getClient().close();
    }
}
