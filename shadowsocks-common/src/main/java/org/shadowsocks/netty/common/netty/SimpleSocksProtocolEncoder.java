package org.shadowsocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

public class SimpleSocksProtocolEncoder extends MessageToByteEncoder<SimpleSocksCmdRequest> {


    @Override
    protected void encode(ChannelHandlerContext ctx, SimpleSocksCmdRequest msg, ByteBuf out) throws Exception {
        msg.write(out);
    }
}
