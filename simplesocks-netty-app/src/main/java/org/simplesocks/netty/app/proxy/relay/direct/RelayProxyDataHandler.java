package org.simplesocks.netty.app.proxy.relay.direct;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


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
