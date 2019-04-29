package org.shadowsocks.netty.common.netty;

import io.netty.util.concurrent.Promise;
import org.shadowsocks.netty.common.protocol.ProxyRequest;

import java.nio.channels.Channel;

public abstract class BaseRelayClient implements RelayClient{

    protected boolean isConnect;


    @Override
    public boolean isConnect() {
        return isConnect;
    }


}
