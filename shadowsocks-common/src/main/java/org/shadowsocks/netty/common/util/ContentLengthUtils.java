package org.shadowsocks.netty.common.util;


public class ContentLengthUtils {


    public static byte[] integerToByte(int target){
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte)((target>>i*8) & 0xFF);
        }
        return result;
    }

    public static int byteToInteger(byte[] bytes){
        int len = bytes.length;
        if(len!=4){
            throw new IllegalArgumentException("length greater than 4");
        }
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value<<=8;
            value|=(bytes[3-i] & 0xFF);
        }
        return value;
    }



}
