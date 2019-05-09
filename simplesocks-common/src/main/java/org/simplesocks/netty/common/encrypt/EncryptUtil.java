package org.simplesocks.netty.common.encrypt;

import java.util.Random;

public class EncryptUtil {


    public static byte[] generateIV(String type){
        int len =0;
        if(type.equalsIgnoreCase("offset")){
            len = 1;
        }else if(type.contains("aes")){
            len = 16;
        }
        Random random = new Random();
        byte[] bytes = new byte[len];
        random.nextBytes(bytes);
        return bytes;

    }
}
