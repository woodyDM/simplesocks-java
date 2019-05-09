package org.simplesocks.netty.common.encrypt;


import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class OffsetEncrypter implements Encrypter {


    private static final OffsetEncrypter instance = new OffsetEncrypter((byte)17);

    public static OffsetEncrypter getInstance(){
        return instance;
    }


    private byte offset;

    public OffsetEncrypter(byte offset) {
        this.offset = offset;
    }

    @Override
    public byte[] encrypt(byte[] rawBytes) {
        byte[] result = new byte[rawBytes.length];
        for (int i = 0; i < rawBytes.length; i++) {
            result[i] =(byte)(rawBytes[i]+offset);
        }
        return result;
    }

    @Override
    public byte[] decrypt(byte[] encodedBytes) {
        byte[] result = new byte[encodedBytes.length];
        for (int i = 0; i < encodedBytes.length; i++) {
            result[i] =(byte)(encodedBytes[i]-offset);
        }
        return result;
    }




}
