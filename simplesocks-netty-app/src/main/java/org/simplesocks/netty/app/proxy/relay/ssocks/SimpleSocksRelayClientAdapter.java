package org.simplesocks.netty.app.proxy.relay.ssocks;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import org.simplesocks.netty.client.SimpleSocksProtocolClient;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.protocol.DataType;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;
import org.simplesocks.netty.common.protocol.ServerResponseMessage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class SimpleSocksRelayClientAdapter implements RelayClient {

    private SimpleSocksProtocolClient client;
    private RelayClientManager manager;

    public SimpleSocksRelayClientAdapter(SimpleSocksProtocolClient client,RelayClientManager manager) {
        this.client = client;
        this.manager = manager;
    }

    @Override
    public boolean isConnect() {
        return client.isConnected();
    }


    @Override
    public Promise<Channel> sendProxyRequest(String host, int port, ConnectionMessage.Type proxyType, EventExecutor eventExecutor) {
        return client.sendProxyRequest(host,port,proxyType,eventExecutor);
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
    public void setReceiveProxyDataAction(Consumer<byte[]> action) {
        client.setProxyDataRequestConsumer((request -> {
            action.accept(request.getData());
        }));
    }

    @Override
    public void setReceiveRemoteResponseAction(BiConsumer<DataType, ServerResponseMessage.Code> action) {
        client.setServerResponseConsumer((response -> {
            action.accept(response.getType(), response.getCode());
        }));
    }

    @Override
    public RelayClientManager manager() {
        return manager;
    }
}
