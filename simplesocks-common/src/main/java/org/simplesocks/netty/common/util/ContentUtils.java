package org.simplesocks.netty.common.util;


import io.netty.buffer.ByteBuf;

public class ContentUtils {



    public static void checkBitLength(String msg,int len){
        if(len>Byte.MAX_VALUE){
            throw new IllegalArgumentException(msg+" exceed max byte length.");
        }
    }

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



    public static byte[] getAllBytes(ByteBuf byteBuf){
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);
        return bytes;
    }




}
