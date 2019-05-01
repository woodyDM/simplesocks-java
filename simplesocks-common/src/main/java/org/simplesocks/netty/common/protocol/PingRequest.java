package org.simplesocks.netty.common.protocol;

public class PingRequest extends StringRequest {


    private static final PingRequest instance = new PingRequest(DataType.PING, "PING");

    private PingRequest(DataType type, String msg) {
        super(type, msg);
    }

    public static PingRequest getInstance(){
        return instance;
    }
}
