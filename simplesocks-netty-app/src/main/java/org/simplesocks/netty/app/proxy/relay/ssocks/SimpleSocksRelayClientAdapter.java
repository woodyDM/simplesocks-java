package org.simplesocks.netty.app.proxy.relay.ssocks;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import org.simplesocks.netty.client.SimpleSocksProtocolClient;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;

import java.util.function.Consumer;

@Getter
public class SimpleSocksRelayClientAdapter implements RelayClient {

    private SimpleSocksProtocolClient client;

    public SimpleSocksRelayClientAdapter(SimpleSocksProtocolClient client ) {
        this.client = client;
    }

    @Override
    public boolean isConnect() {
        return client.isConnected();
    }


    @Override
    public Promise<Channel> sendProxyRequest(String host, int port, ConnectionMessage.Type proxyType, EventExecutor eventExecutor) {
        return client.sendProxyRequest(host, port, proxyType, eventExecutor);
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void sendProxyData(byte[] data) {
        ProxyDataMessage request = new ProxyDataMessage(data);
        client.sendProxyData(request);
    }

    @Override
    public void onReceiveProxyData(Consumer<byte[]> action) {
        client.setProxyDataRequestConsumer((request -> {
            action.accept(request.getData());
        }));
    }


    @Override
    public void onClose(Runnable action) {
        client.onClose(action);
    }


}
