package org.simplesocks.netty.common.encrypt.encrypter;


import org.simplesocks.netty.common.encrypt.Encrypter;

public class CaesarEncrypter implements Encrypter {


    private byte offset;

    public CaesarEncrypter(byte offset) {
        this.offset = offset;
    }

    public byte getOffset() {
        return offset;
    }

    @Override
    public byte[] encrypt(byte[] plain) {
        byte[] result = new byte[plain.length];
        for (int i = 0; i < plain.length; i++) {
            result[i] =(byte)(plain[i]+offset);
        }
        return result;
    }

    @Override
    public byte[] decrypt(byte[] encryptedBytes) {
        byte[] result = new byte[encryptedBytes.length];
        for (int i = 0; i < encryptedBytes.length; i++) {
            result[i] =(byte)(encryptedBytes[i]-offset);
        }
        return result;
    }




}
