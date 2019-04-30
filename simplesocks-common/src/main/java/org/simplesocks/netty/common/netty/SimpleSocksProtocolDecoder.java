package org.simplesocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.simplesocks.netty.common.protocol.CmdRequestFactory;
import org.simplesocks.netty.common.protocol.SimpleSocksCmdRequest;

import java.util.List;

public class SimpleSocksProtocolDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        SimpleSocksCmdRequest request = CmdRequestFactory.newInstance(byteBuf);
        list.add(request);
    }
}
