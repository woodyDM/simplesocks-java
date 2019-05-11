package org.simplesocks.netty.common.encrypt.factory;

import org.simplesocks.netty.common.encrypt.Encrypter;

public interface EncrypterFactory<T extends Encrypter> {

    void registerKey(byte[] appKey);

    boolean support(String encType);

    T newInstant(String encType, byte[] iv);

    byte[] randomIv(String encType);

}
