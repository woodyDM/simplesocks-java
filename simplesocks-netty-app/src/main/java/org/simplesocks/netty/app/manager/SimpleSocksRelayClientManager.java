package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.proxy.relay.ssocks.SimpleSocksRelayClientAdapter;
import org.simplesocks.netty.client.SimpleSocksProtocolClient;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.ConnectionMessage;


@Slf4j
public class SimpleSocksRelayClientManager implements RelayClientManager {

    private String host;
    private int port;
    private String auth;
    private EventLoopGroup loopGroup;
    private String encType = "Offset";


    public SimpleSocksRelayClientManager(String host, int port, String auth, EventLoopGroup loopGroup ) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.loopGroup = loopGroup;

    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {
        SimpleSocksProtocolClient client = new SimpleSocksProtocolClient(auth, encType, host, port, loopGroup);
        SimpleSocksRelayClientAdapter adapter = new SimpleSocksRelayClientAdapter(client, this);
        Promise<RelayClient> promise = eventExecutor.newPromise();
        client.init().addListener(f1->{
            if(f1.isSuccess()){
                ConnectionMessage.Type type = ConnectionMessage.Type.valueOf(socksCmdRequest.addressType().byteValue());
                client.sendProxyRequest(socksCmdRequest.host(), socksCmdRequest.port(), type, eventExecutor)
                        .addListener(f2->{
                            if(f2.isSuccess()){
                                promise.setSuccess(adapter);
                            }else{
                                promise.setFailure(f2.cause());
                            }
                        });
            }else{
                promise.setFailure(f1.cause());
            }
        });
        return promise;
    }

    @Override
    public void returnClient(RelayClient client) {
        SimpleSocksRelayClientAdapter adapter = (SimpleSocksRelayClientAdapter)client;
        adapter.getClient().close();
    }
}
