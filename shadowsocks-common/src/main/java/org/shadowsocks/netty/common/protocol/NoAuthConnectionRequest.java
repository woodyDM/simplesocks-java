package org.shadowsocks.netty.common.protocol;

public class NoAuthConnectionRequest extends ByteBasedRequest {



    public NoAuthConnectionRequest() {
        super(DataType.CONNECT);

    }

    @Override
    protected byte[] body() {
        byte[] bytes = new byte[1];
        bytes[0] = Constants.NO_AUTH;
        return bytes;
    }

    @Override
    public String toString() {
        return "NoAuthConnectionRequest{}";
    }
}
