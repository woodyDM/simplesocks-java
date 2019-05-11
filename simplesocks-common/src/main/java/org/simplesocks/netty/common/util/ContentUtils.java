package org.simplesocks.netty.common.util;




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



}
