package org.simplesocks.netty.common.netty;

import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.simplesocks.netty.common.protocol.ConnectionMessage;

import java.util.function.Consumer;

/**
 * abstraction for relay task client.
 */
public interface RelayClient {

    boolean isConnect();

    /**
     * client try to proxy
     * @param host
     * @param port
     * @param proxyType
     * @param eventExecutor
     * @return
     */
    Promise<Channel> sendProxyRequest(String host, int port, ConnectionMessage.Type proxyType, EventExecutor eventExecutor);

    void close();

    void onClose(Runnable action);

    void sendProxyData(byte[] data);

    void onReceiveProxyData(Consumer<byte[]> action);

}
