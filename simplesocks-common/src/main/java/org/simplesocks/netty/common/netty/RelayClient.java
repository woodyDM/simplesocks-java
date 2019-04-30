package org.simplesocks.netty.common.netty;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.simplesocks.netty.common.protocol.DataType;
import org.simplesocks.netty.common.protocol.ProxyRequest;
import org.simplesocks.netty.common.protocol.ServerResponse;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * abstraction for relay task client.
 */
public interface RelayClient {


    boolean isConnect();


    Promise<Channel> sendProxyRequest(String host, int port, ProxyRequest.Type proxyType, EventExecutor eventExecutor);


    Promise<Void> endProxy(EventExecutor eventExecutor);


    void sendProxyData(byte[] data);


    void setReceiveProxyDataAction(Consumer<byte[]> action);


    void setReceiveRemoteResponseAction(BiConsumer<DataType,ServerResponse.Code> action);


}
