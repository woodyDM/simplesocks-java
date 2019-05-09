package org.simplesocks.netty.common.encrypt.factory;


import org.simplesocks.netty.common.encrypt.Encrypter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**

 *
 */
public abstract class AbstractEncrypterFactory<T extends Encrypter> implements EncrypterFactory<T> {

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

    private byte[] getPaddingKey(byte[] raw, int target){
        byte[] bytes = new byte[target];
        int len = raw.length;
        int targetLen = len<target ? len : target;
        System.arraycopy(raw,0,bytes,0, targetLen);
        for (int i = 0; i < target - len; i++) {
            bytes[len+i] = (byte)i;
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




}
