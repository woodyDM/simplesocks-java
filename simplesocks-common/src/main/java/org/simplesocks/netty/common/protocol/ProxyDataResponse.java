package org.simplesocks.netty.common.protocol;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

@Getter
public class ProxyDataResponse extends ServerResponseMessage {

    private String id;

    public ProxyDataResponse(Code code, String id) {
        super(DataType.PROXY_DATA_RESPONSE, code);
        this.id = id;
    }

    @Override
    protected byte[][] body() {
        byte[] head = new byte[2];
        byte[] idBytes = id.getBytes(StandardCharsets.UTF_8);
        head[0] = code.bit;
        head[1] = (byte)idBytes.length;
        byte[][] r = new byte[2][];
        r[0] = head;
        r[1] = idBytes;
        return r;
    }
}
