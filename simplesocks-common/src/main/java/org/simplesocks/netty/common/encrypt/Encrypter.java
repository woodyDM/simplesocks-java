package org.simplesocks.netty.common.encrypt;

public interface Encrypter {


    byte[] encrypt(byte[] rawBytes);

    byte[] decrypt(byte[] encodedBytes);



}
