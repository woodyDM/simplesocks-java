package org.simplesocks.netty.common.protocol;

import java.util.NoSuchElementException;

public enum DataType {

    CONNECT(0x01),
    PROXY(0x02),
    PROXY_DATA(0x03),
    END_PROXY(0x04),
    PING(0x05),

    CONNECT_RESPONSE(0x11),
    PROXY_RESPONSE(0x12),
    PROXY_DATA_RESPONSE(0x13),
    END_PROXY_RESPONSE(0x14),
    PONG(0x15),
    END_CONNECTION(0x16);
    private byte bit;

    DataType(int bit){
        this.bit = (byte)bit;
    }

    public byte getBit() {
        return bit;
    }

    public static DataType parseByte(byte b){
        for(DataType t:values()){
            if(b==t.bit)
                return t;
        }
        throw new NoSuchElementException("DataType not found . bit is "+b);
    }

    public DataType toResponse(){
        byte b =(byte)( bit | 0x10);
        return parseByte(b);
    }


}
