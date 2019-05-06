package org.simplesocks.netty.common.netty;

import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

public interface RelayClientManager {

    /**
     * borrow a client, return RelayClient if the client can proxy the request.
     * @param eventExecutor
     * @param socksCmdRequest
     * @return
     */
    Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest);


    void returnClient(RelayClient client);

}
