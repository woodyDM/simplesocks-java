package org.simplesocks.netty.common.protocol;

public class PongRequest extends StringRequest {


    private static final PongRequest instance = new PongRequest(DataType.PONG, "PONG");

    private PongRequest(DataType type, String msg) {
        super(type, msg);
    }

    public static PongRequest getInstance(){
        return instance;
    }
}
