package org.simplesocks.netty.common.netty;

public abstract class BaseRelayClient implements RelayClient{

    protected boolean isConnect;


    @Override
    public boolean isConnect() {
        return isConnect;
    }


}
