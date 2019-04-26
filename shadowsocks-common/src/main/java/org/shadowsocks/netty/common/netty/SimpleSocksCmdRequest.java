package org.shadowsocks.netty.common.netty;

import io.netty.buffer.ByteBuf;

public interface SimpleSocksCmdRequest {

    void write(ByteBuf buf);


}
