package org.simplesocks.netty.common.encrypt.factory;



import org.simplesocks.netty.common.encrypt.EncType;
import org.simplesocks.netty.common.encrypt.encrypter.AesEncrypter;

/**
 * support
 * aes-cbc
 * aes-cfb
 * where length might be 128 192 256 depend on appkey
 *
 */
public class AesFactory extends AbstractEncrypterFactory<AesEncrypter> {


    public static final String TYPE_CBC = EncType.AES_CBC.getEncName();
    public static final String TYPE_CFB = EncType.AES_CFB.getEncName();

    @Override
    protected void addSupport() {
        addSupport(TYPE_CBC);
        addSupport(TYPE_CFB);
    }

    @Override
    public AesEncrypter newInstant(String encType, byte[] iv) {
        return new AesEncrypter(encType, appKey, iv );
    }

    @Override
    public byte[] randomIv(String encType) {
        return randomBytes(16);
    }
}
