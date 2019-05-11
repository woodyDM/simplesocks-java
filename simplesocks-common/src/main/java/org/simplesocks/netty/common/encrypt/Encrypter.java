package org.simplesocks.netty.common.encrypt;

public interface Encrypter {


    byte[] encrypt(byte[] plain);

    byte[] decrypt(byte[] encryptedBytes);



}
