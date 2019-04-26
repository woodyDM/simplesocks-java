package org.shadowsocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import org.shadowsocks.netty.common.protocol.DataType;
import org.shadowsocks.netty.common.protocol.SimpleSocksCmdRequest;

import java.nio.charset.StandardCharsets;

public class StringCmdRequest implements SimpleSocksCmdRequest {

    private String string ;

    public StringCmdRequest(String string) {
        this.string = string;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(0x01);
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        int len = 1+4+bytes.length;
        buf.writeInt(len);
        buf.writeBytes(bytes);
    }

    @Override
    public DataType getType() {
        return null;
    }
}
