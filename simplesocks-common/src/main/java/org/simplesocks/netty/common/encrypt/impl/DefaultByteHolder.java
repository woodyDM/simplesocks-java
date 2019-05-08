package org.simplesocks.netty.common.encrypt.impl;

import lombok.Getter;
import org.omg.CORBA.ByteHolder;
import org.simplesocks.netty.common.encrypt.BytesHolder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
public class DefaultByteHolder implements BytesHolder {

    private byte[] data;
    private int len;

    public DefaultByteHolder(byte[] data, int len) {
        this.data = data;
        this.len = len;
        if(len>data.length){
            throw new IllegalArgumentException("len > data.length."+len+" > "+data.length);
        }
    }

    public DefaultByteHolder(byte[] data) {
        this.data = data;
        this.len = data.length;
    }

    @Override
    public byte[] getAllBytes() {
        return data;
    }

    @Override
    public int length() {
        return len;
    }

    public static DefaultByteHolder valueOf(String s){
        return new DefaultByteHolder(s.getBytes(StandardCharsets.UTF_8));
    }


}
