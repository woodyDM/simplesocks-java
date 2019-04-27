package org.shadowsocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class ProxyDataRequest implements SimpleSocksCmdRequest {

    private ByteBuf incomingBuf;

    public ProxyDataRequest(ByteBuf incomingBuf) {

        this.incomingBuf = incomingBuf;
    }

    @Override
    public void write(ByteBuf buf) {
        int len = incomingBuf.readableBytes();
        try{
            buf.writeByte(Constants.VERSION1);
            buf.writeInt(len + Constants.LEN_HEAD);
            buf.writeByte(getType().getBit());
            buf.writeBytes(incomingBuf);
        }finally {
            ReferenceCountUtil.release(incomingBuf);
        }
    }

    @Override
    public DataType getType() {
        return DataType.PROXY_DATA;
    }
}
