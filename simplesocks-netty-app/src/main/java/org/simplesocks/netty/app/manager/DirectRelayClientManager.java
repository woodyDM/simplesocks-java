package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.simplesocks.netty.app.proxy.relay.direct.DirectRelayClient;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;

public class DirectRelayClientManager implements RelayClientManager {

    EventLoopGroup group;

    public DirectRelayClientManager(EventLoopGroup group) {
        this.group = group;
    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor) {
        Promise<RelayClient> promise = eventExecutor.newPromise();
        DirectRelayClient directRelayClient = new DirectRelayClient(group, this);
        promise.setSuccess(directRelayClient);
        return promise;
    }

    @Override
    public void returnClient(RelayClient client) {
        DirectRelayClient c=(DirectRelayClient)client;
        c.close();
    }

}
