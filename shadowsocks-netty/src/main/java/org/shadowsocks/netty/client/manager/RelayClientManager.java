package org.shadowsocks.netty.client.manager;

import io.netty.util.concurrent.Promise;
import org.shadowsocks.netty.common.netty.RelayClient;

public interface RelayClientManager {

    Promise<RelayClient> borrowerOne();

    void returnOne(RelayClient client);

}
