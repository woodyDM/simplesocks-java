package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;

public interface SimpleSocksMessage {

    void write(ByteBuf buf);

    DataType getType();

}
