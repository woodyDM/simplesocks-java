package org.simplesocks.netty.common.encrypt.factory;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;

import java.io.RandomAccessFile;
import java.util.Random;

@Slf4j
public class OffsetFactory implements EncrypterFactory<OffsetEncrypter> {

    public static final String TYPE = "offset";

    @Override
    public void registerKey(byte[] appKey) {
        //ignore
    }

    @Override
    public boolean support(String encType) {
        return TYPE.equalsIgnoreCase(encType);
    }

    @Override
    public OffsetEncrypter newInstant(String encType, byte[] iv) {
        if(iv==null||iv.length==0){
            throw new IllegalArgumentException("invalid iv!");
        }
        if(iv.length>1){
            log.warn("Offset only need 1 byte.");
        }
        return new OffsetEncrypter(iv[0]);
    }

    @Override
    public byte[] randomIv(String encType) {
        byte[] b = new byte[1];
        new Random().nextBytes(b);
        return b;
    }
}
