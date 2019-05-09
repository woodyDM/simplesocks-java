package org.simplesocks.netty.common.encrypt.factory;


import org.simplesocks.netty.common.encrypt.Encrypter;

import java.util.ArrayList;
import java.util.List;

public class CompositeFactory implements EncrypterFactory  {
    
    private List<EncrypterFactory> factories = new ArrayList<>();

    public CompositeFactory() {
        factories.add(new AesFactory());
        factories.add(new OffsetFactory());
    }

    @Override
    public void registerKey(byte[] appKey) {
        factories.forEach(f->f.registerKey(appKey));
    }

    @Override
    public boolean support(String encType) {
        for(EncrypterFactory f:factories){
            if(f.support(encType))
                return true;
        }
        throw new IllegalArgumentException("Type "+encType+" not supported");
    }

    @Override
    public Encrypter newInstant(String encType, byte[] iv) {
        for(EncrypterFactory f:factories){
            if(f.support(encType))
                return f.newInstant(encType,iv);
        }
        throw new IllegalArgumentException("Type "+encType+" not supported");
    }
    
}
