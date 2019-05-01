package org.simplesocks.netty.common.protocol;

import java.nio.charset.StandardCharsets;

public class StringRequest extends ByteBasedRequest {

    String msg;

    public StringRequest(DataType type, String msg) {
        super(type);
        this.msg = msg;
    }

    @Override
    protected byte[] body() {
        return msg.getBytes(StandardCharsets.UTF_8);
    }
}
