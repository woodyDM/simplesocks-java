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

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {
        Promise<RelayClient> promise = eventExecutor.newPromise();
        DirectRelayClient directRelayClient = new DirectRelayClient(group, this);
        ConnectionMessage.Type type = ConnectionMessage.Type.valueOf(socksCmdRequest.addressType().byteValue());
        directRelayClient.sendProxyRequest(socksCmdRequest.host(), socksCmdRequest.port(), type, eventExecutor)
                .addListener(future -> {
                    if(future.isSuccess()){
                        promise.setSuccess(directRelayClient);
                    }else{
                        promise.setFailure(new BaseSystemException("Failed to connect to "+socksCmdRequest.host()+":" + socksCmdRequest.port()));
                    }
                });
        return promise;
    }

    @Override
    public void returnClient(RelayClient client) {
        DirectRelayClient c=(DirectRelayClient)client;
        c.close();
    }

}
