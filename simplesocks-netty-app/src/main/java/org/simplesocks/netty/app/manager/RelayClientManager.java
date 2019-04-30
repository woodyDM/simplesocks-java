package org.simplesocks.netty.app.manager;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.simplesocks.netty.common.netty.RelayClient;

public interface RelayClientManager {


    Promise<RelayClient> borrow(EventExecutor eventExecutor);

    void returnClient(RelayClient client);

}
