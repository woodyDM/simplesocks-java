package org.simplesocks.netty.common.util;


import io.netty.buffer.ByteBuf;

public class ContentUtils {



    public static void checkBitLength(String msg,int len){
        if(len>Byte.MAX_VALUE){
            throw new IllegalArgumentException(msg+" exceed max byte length.");
        }
    }

    public static byte[] shortToByte(int target){
        byte[] result = new byte[2];
        result[0] = (byte) (target>>>8);
        result[1] = (byte) target ;
        return result;
    }

    public static void main(String[] args) {
        int s =  60215;
        byte[] bytes = shortToByte(s);
        short i = byteToShort(bytes);
        System.out.println(i);
    }

    public static short byteToShort(byte[] bytes){
        int len = bytes.length;
        if(len!=2){
            throw new IllegalArgumentException("length greater than 4");
        }
        return (short) (bytes[0] << 8 | bytes[1] & 0xFF);
    }



    public static byte[] getAllBytes(ByteBuf byteBuf){
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);
        return bytes;
    }




}
