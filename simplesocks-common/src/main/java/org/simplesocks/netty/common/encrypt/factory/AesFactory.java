package org.simplesocks.netty.common.encrypt.factory;



import org.simplesocks.netty.common.encrypt.AesEncrypter;

/**
 * support
 * aes-cbc-x
 * aes-cfb-x
 * where x might be 128 192 256 depend on appkey
 *
 */
public class AesFactory extends AbstractEncrypterFactory<AesEncrypter> {

    @Override
    protected void addSupport() {
        addSupport("aes-cbc");
        addSupport("aes-cfb");
    }

    @Override
    public AesEncrypter newInstant(String encType, byte[] iv) {

        return new AesEncrypter(encType, appKey, iv );
    }



}
