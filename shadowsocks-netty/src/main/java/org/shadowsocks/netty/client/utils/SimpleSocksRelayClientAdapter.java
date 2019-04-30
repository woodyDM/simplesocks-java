package org.shadowsocks.netty.client.utils;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import org.shadowsocks.netty.common.netty.RelayClient;
import org.shadowsocks.netty.common.protocol.DataType;
import org.shadowsocks.netty.common.protocol.ProxyDataRequest;
import org.shadowsocks.netty.common.protocol.ProxyRequest;
import org.shadowsocks.netty.common.protocol.ServerResponse;
import org.shadowsocks.netty.server.SimpleSocksProtocolClient;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class SimpleSocksRelayClientAdapter implements RelayClient {

    private SimpleSocksProtocolClient client;

    public SimpleSocksRelayClientAdapter(SimpleSocksProtocolClient client) {
        this.client = client;
    }

    @Override
    public boolean isConnect() {
        return client.isConnected();
    }

    @Override
    public Promise<Channel> sendProxyRequest(String host, int port, ProxyRequest.Type proxyType, EventExecutor eventExecutor) {
        return client.sendProxyRequest(host, port, proxyType);
    }

    @Override
    public Promise<Void> endProxy(EventExecutor eventExecutor) {
        return client.endProxy();
    }

    @Override
    public void sendProxyData(byte[] data) {
        ProxyDataRequest request = new ProxyDataRequest(data);
        client.sendProxyData(request);
    }

    @Override
    public void setReceiveProxyDataAction(Consumer<byte[]> action) {
        client.setProxyDataRequestConsumer((request -> {
            action.accept(request.getBytes());
        }));
    }

    @Override
    public void setReceiveRemoteResponseAction(BiConsumer<DataType, ServerResponse.Code> action) {
        client.setServerResponseConsumer((response -> {
            action.accept(response.getType(), response.getCode());
        }));
    }
}
