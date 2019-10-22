package org.simplesocks.netty.common.encrypt.factory;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.EncType;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.EncrypterFactory;
import org.simplesocks.netty.common.encrypt.encrypter.CaesarEncrypter;

import java.util.Random;

@Slf4j
public class CaesarFactory implements EncrypterFactory {

    public static final String TYPE = EncType.CAESAR.getEncName();

    @Override
    public void registerKey(byte[] appKey) {
        //ignore
    }

    @Override
    public boolean support(String encType) {
        return TYPE.equalsIgnoreCase(encType);
    }

    @Override
    public Encrypter newInstant(String encType, byte[] iv) {
        if(iv==null||iv.length==0){
            throw new IllegalArgumentException("invalid iv!");
        }
        if(iv.length>1){
            log.warn("Caesar encrypter only need 1 byte.");
        }
        return new CaesarEncrypter(iv[0]);
    }

    @Override
    public byte[] randomIv(String encType) {
        byte[] b = new byte[1];
        new Random().nextBytes(b);
        return b;
    }
}
