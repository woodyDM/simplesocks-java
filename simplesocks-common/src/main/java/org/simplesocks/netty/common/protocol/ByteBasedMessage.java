package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ByteBasedMessage implements SimpleSocksMessage {

    protected DataType type;

    public ByteBasedMessage(DataType type){
        this.type = type;
    }

    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public void write(ByteBuf buf) {
        byte[][] body = body();
        int len = 0;
        for (byte[]it:body) len+=it.length;
        buf.writeByte(Constants.VERSION1);
        buf.writeInt(len + Constants.LEN_HEAD);
        buf.writeByte(type.getBit());
        if(len>0){
            for(byte[] it:body){
                if(it.length>0) buf.writeBytes(it);
            }
        }
    }

    abstract protected byte[][] body();

}
