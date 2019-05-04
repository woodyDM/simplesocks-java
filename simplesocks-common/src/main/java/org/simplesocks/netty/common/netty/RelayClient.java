package org.simplesocks.netty.common.netty;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.simplesocks.netty.common.protocol.ConnectionMessage;
import org.simplesocks.netty.common.protocol.DataType;
import org.simplesocks.netty.common.protocol.ServerResponseMessage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * abstraction for relay task client.
 */
public interface RelayClient {


    boolean isConnect();


    Promise<Channel> sendProxyRequest(String host, int port, ConnectionMessage.Type proxyType, EventExecutor eventExecutor);


    void close();


    void sendProxyData(byte[] data);


    void setReceiveProxyDataAction(Consumer<byte[]> action);


    void setReceiveRemoteResponseAction(BiConsumer<DataType, ServerResponseMessage.Code> action);


    RelayClientManager manager();


}
