package org.simplesocks.netty.common.netty;

import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

public interface RelayClientManager {

    /**
     * borrow a client
     * @param eventExecutor
     * @param socksCmdRequest
     * @return
     */
    Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest);


    void returnClient(RelayClient client);

}
