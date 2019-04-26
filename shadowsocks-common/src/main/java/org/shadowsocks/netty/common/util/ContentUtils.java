package org.shadowsocks.netty.common.util;


import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ContentUtils {





    public static byte[] shortToByte(short target){
        byte[] result = new byte[2];
        for (int i = 0; i < 2; i++) {
            result[1-i] = (byte)((target>>i*8) & 0xFF);
        }
        return result;
    }

    public static short byteToShort(byte[] bytes){
        int len = bytes.length;
        if(len!=2){
            throw new IllegalArgumentException("length greater than 4");
        }
        short value = 0;
        for (int i = 0; i < 2; i++) {
            value<<=8;
            value|=(bytes[i] & 0xFF);
        }
        return value;
    }

    public static String leftBytesToString(ByteBuf byteBuf){
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static void main(String[] args) {
        byte[] bytes = shortToByte((short)895);
        System.out.println(byteToShort(bytes));
        System.out.println(".");
    }


}
