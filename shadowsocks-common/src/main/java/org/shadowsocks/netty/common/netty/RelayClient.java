package org.shadowsocks.netty.common.netty;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Promise;
import org.shadowsocks.netty.common.protocol.DataType;
import org.shadowsocks.netty.common.protocol.ProxyRequest;
import org.shadowsocks.netty.common.protocol.ServerResponse;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * abstraction for relay task client.
 */
public interface RelayClient {


    boolean isConnect();


    Promise<Channel> sendProxyRequest(String host, int port, ProxyRequest.Type proxyType);


    Promise<Void> endProxy();


    void sendProxyData(byte[] data);


    void setReceiveProxyDataAction(Consumer<byte[]> action);


    void setReceiveRemoteResponseAction(BiConsumer<DataType,ServerResponse.Code> action);


}
