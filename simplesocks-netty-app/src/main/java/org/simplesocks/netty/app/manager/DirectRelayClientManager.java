package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.simplesocks.netty.app.proxy.relay.direct.DirectRelayClient;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.protocol.ConnectionMessage;

public class DirectRelayClientManager implements RelayClientManager {

    private EventLoopGroup group;

    public DirectRelayClientManager(EventLoopGroup group) {
        this.group = group;
    }

    /**
     * create direct client always success.
     * @param eventExecutor
     * @param socksCmdRequest
     * @return
     */
    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {
        Promise<RelayClient> promise = eventExecutor.newPromise();
        DirectRelayClient directRelayClient = new DirectRelayClient(group);
        promise.setSuccess(directRelayClient);
        return promise;
    }

    @Override
    public void returnClient(RelayClient client) {
        DirectRelayClient c=(DirectRelayClient)client;
        c.close();
    }

}
