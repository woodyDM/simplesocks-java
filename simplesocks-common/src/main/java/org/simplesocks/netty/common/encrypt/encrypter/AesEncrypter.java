package org.simplesocks.netty.common.encrypt.encrypter;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.exception.BaseSystemException;

@Slf4j
public class AesEncrypter implements Encrypter {

    private Type type;
    private byte[] appKey;
    private byte[] iv;
    private static final int VECTOR_SIZE = 16;


    public AesEncrypter(String encType, byte[] appKey, byte[] iv) {
        if (iv.length != VECTOR_SIZE) {
            throw new IllegalArgumentException("iv length should be 16");
        }
        this.type = Type.of(encType);
        this.appKey = appKey;
        this.iv = iv;
    }


    @Override
    public byte[] encrypt(byte[] plain) {
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(type.cipher(new AESLightEngine()));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(appKey), iv);
        aes.init(true, ivAndKey);
        byte[] result = cipherData(aes, plain);
        if (type == Type.CFB && result.length < 16) {
            log.info("Size found: before {} after {}.", plain.length, result.length);
        }
        return result;
    }

    @Override
    public byte[] decrypt(byte[] encryptedBytes) {


        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(type.cipher(new AESLightEngine()));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(appKey),
                iv);
        aes.init(false, ivAndKey);
        return cipherData(aes, encryptedBytes);
    }


    private static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data) {
        try {
            int minSize = cipher.getOutputSize(data.length);
            byte[] outBuf = new byte[minSize];
            int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
            int length2 = cipher.doFinal(outBuf, length1);
            int actualLength = length1 + length2;
            byte[] result = new byte[actualLength];
            System.arraycopy(outBuf, 0, result, 0, result.length);
            return result;
        } catch (InvalidCipherTextException e) {
            throw new BaseSystemException("Fail in encrypter. data size is " + data.length, e);
        }

    }


    public enum Type {
        CBC,
        CFB;


        public static Type of(String encType) {
            for (Type it : values()) {
                if (encType.contains(it.name().toLowerCase())) {
                    return it;
                }
            }
            throw new IllegalArgumentException("no type found for " + encType);
        }

        public BlockCipher cipher(BlockCipher engine) {
            switch (this) {
                case CBC:
                    return new CBCBlockCipher(engine);
                case CFB:
                    return new CFBBlockCipher(engine, VECTOR_SIZE);
                default:
                    throw new IllegalStateException("Type no cipher");
            }
        }
    }
}
