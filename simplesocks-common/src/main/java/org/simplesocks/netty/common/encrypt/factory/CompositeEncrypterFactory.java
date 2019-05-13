package org.simplesocks.netty.common.encrypt.factory;


import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.EncrypterFactory;

import java.util.ArrayList;
import java.util.List;

public class CompositeEncrypterFactory implements EncrypterFactory {
    
    private List<EncrypterFactory> factories = new ArrayList<>();


    public CompositeEncrypterFactory() {
        factories.add(new AesFactory());
        factories.add(new CaesarFactory());
    }

    @Override
    public void registerKey(byte[] appKey) {
        factories.forEach(f->f.registerKey(appKey));
    }

    @Override
    public boolean support(String encType) {
        return get(encType)!=null;
    }

    @Override
    public Encrypter newInstant(String encType, byte[] iv) {
        return get(encType).newInstant(encType, iv);

    }

    @Override
    public byte[] randomIv(String encType) {
        return get(encType).randomIv(encType);
    }


    private EncrypterFactory get(String encType){
        for(EncrypterFactory f:factories){
            if(f.support(encType))
                return f;
        }
        throw new IllegalArgumentException("Type "+encType+" not supported");
    }
}
