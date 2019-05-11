package org.simplesocks.netty.common.protocol;

import java.util.NoSuchElementException;

public enum DataType {

    CONNECT (0x01),
    PROXY_DATA(0x02),
    CONNECT_RESPONSE(0x11),
    PROXY_DATA_RESPONSE(0x12);

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
        throw new NoSuchElementException("DataType not found .Bit is "+b);
    }

}
