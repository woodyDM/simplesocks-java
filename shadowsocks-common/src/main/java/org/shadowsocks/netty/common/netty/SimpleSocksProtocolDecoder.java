package org.shadowsocks.netty.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.shadowsocks.netty.common.protocol.CmdRequestFactory;
import org.shadowsocks.netty.common.protocol.SimpleSocksCmdRequest;

import java.util.List;

public class SimpleSocksProtocolDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        SimpleSocksCmdRequest request = CmdRequestFactory.newInstance(byteBuf);
        if(request!=null){
            list.add(request);
        }
    }
}
