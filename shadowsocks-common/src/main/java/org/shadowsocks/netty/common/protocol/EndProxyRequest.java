package org.shadowsocks.netty.common.protocol;

public class EndProxyRequest extends ByteBasedRequest {

    public EndProxyRequest( ) {
        super(DataType.END_PROXY);
    }

    @Override
    protected byte[] body() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "EndProxyRequest ";
    }
}
