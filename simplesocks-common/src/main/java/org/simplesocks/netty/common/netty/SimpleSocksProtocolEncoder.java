package org.simplesocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.simplesocks.netty.common.protocol.SimpleSocksMessage;

public class SimpleSocksProtocolEncoder extends MessageToByteEncoder<SimpleSocksMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SimpleSocksMessage msg, ByteBuf out) throws Exception {
        msg.write(out);
    }
}
