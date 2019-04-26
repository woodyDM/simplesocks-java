package org.shadowsocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;

public interface SimpleSocksCmdRequest {

    void write(ByteBuf buf);

    DataType getType();

}
