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

    /**
     * is client still connect
     * @return
     */
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

    /**
     * close client
     */
    void close();

    /**
     * callback when close
     * @param action
     */
    void onClose(Runnable action);

    /**
     * when sendProxyRequest Promise success ,call this to send data to remote.
     * @param data
     */
    void sendProxyData(byte[] data);

    /**
     * callback when receive data from remote.
     * @param action
     */
    void onReceiveProxyData(Consumer<byte[]> action);

}
