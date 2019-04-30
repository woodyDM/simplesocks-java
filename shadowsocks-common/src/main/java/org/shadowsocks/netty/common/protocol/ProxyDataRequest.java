package org.shadowsocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.ReferenceCountUtil;



public class ProxyDataRequest implements SimpleSocksCmdRequest {

    private ByteBuf incomingBuf;
    private boolean hasArray;
    private byte[] data;
    private int index ;


    public ProxyDataRequest(ByteBuf incomingBuf ) {
        this.incomingBuf = incomingBuf;
        this.hasArray = false;
    }

    public ProxyDataRequest(byte[] incomingBytes) {
        this.data = incomingBytes;
        hasArray = true;
        this.index = 0;
    }

    public ProxyDataRequest(byte[] incomingBytes, int index) {
        this.data = incomingBytes;
        hasArray = true;
        this.index = index;
    }

    public byte[] getBytes() {
        return data;
    }

    @Override
    public String toString() {
        int len = hasArray ? data.length : incomingBuf.readableBytes();
        return "ProxyDataRequest{" +
                "hasArray=" + hasArray + "  len="+len+
                '}';
    }

    public ByteBuf getIncomingBuf() {
        if(hasArray){
            return Unpooled.wrappedBuffer(data, index, data.length-index);
        }else{
            return incomingBuf;
        }
    }

    public int size(){
        return hasArray ? data.length-index : incomingBuf.readableBytes();
    }


    @Override
    public void write(ByteBuf buf) {

        buf.writeByte(Constants.VERSION1);
        buf.writeInt(size() + Constants.LEN_HEAD);
        buf.writeByte(getType().getBit());

        if(hasArray){
            buf.writeBytes(data, index, data.length - index);
        }else{
            try{
                buf.writeBytes(incomingBuf);
            }finally {
                ReferenceCountUtil.release(incomingBuf);
            }
        }
    }

    @Override
    public DataType getType() {
        return DataType.PROXY_DATA;
    }
}
