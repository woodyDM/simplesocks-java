package org.simplesocks.netty.common.encrypt.factory;


import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.EncrypterFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**

 *
 */
public abstract class AbstractEncrypterFactory implements EncrypterFactory  {

    protected byte[] appKey ;
    private final Set<String> supportsEncMethod= new HashSet<>();


    public AbstractEncrypterFactory() {
        addSupport();
    }

    @Override
    public void registerKey(byte[] appKey) {
        Objects.requireNonNull(appKey);
        int len = appKey.length;
        if(len==0){
            throw new IllegalArgumentException("AppKey invalid ,length is 0");
        }else if(len<=16){
            this.appKey = getPaddingKey(appKey, 16);
        }else if(len<=24){
            this.appKey = getPaddingKey(appKey, 24);
        }else{
            this.appKey = getPaddingKey(appKey, 32);
        }
    }

    /**
     * padding appkey to targetLength, using 0
     * @param raw
     * @param target    16 / 24 / 32
     * @return
     */
    public byte[] getPaddingKey(byte[] raw, int target){
        byte[] bytes = new byte[target];
        int len = raw.length;
        System.arraycopy(raw,0,bytes,0, len);
        PKCS7Padding pkcs7Padding = new PKCS7Padding();
        int eff = pkcs7Padding.addPadding(bytes, len);
        if (eff != target - len) {
            throw new IllegalStateException("Should eq");
        }
        return bytes;
    }



    @Override
    public boolean support(String encType) {
        return supportsEncMethod.contains(encType);
    }

    protected void addSupport(String encType){
        supportsEncMethod.add(encType);
    }

    abstract protected void addSupport();


    protected byte[] randomBytes(int len){
        byte[] b = new byte[len];
        new Random().nextBytes(b);
        return b;
    }


}
