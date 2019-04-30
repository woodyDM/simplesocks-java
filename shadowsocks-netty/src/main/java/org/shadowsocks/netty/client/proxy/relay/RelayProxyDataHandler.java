package org.shadowsocks.netty.client.proxy.relay;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.DataType;
import org.shadowsocks.netty.common.protocol.ProxyDataRequest;
import org.shadowsocks.netty.common.protocol.ServerResponse;
import org.shadowsocks.netty.common.protocol.SimpleSocksCmdRequest;

@Slf4j
public class RelayProxyDataHandler extends ChannelInboundHandlerAdapter {

    private DirectRelayClient client;

    public RelayProxyDataHandler(DirectRelayClient client ) {
        this.client = client;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            ByteBuf byteBuf = (ByteBuf)msg;
            if(byteBuf.hasArray()){
                byte[] array = byteBuf.array();
                client.onReceiveProxyData(array);
            }else{
                int len = byteBuf.readableBytes();
                byte[] bytes = new byte[len];
                byteBuf.readBytes(bytes);
                client.onReceiveProxyData(bytes);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }




}
