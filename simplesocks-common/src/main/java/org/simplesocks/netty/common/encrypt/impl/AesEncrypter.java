package org.simplesocks.netty.common.encrypt.impl;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;

import org.simplesocks.netty.common.encrypt.AbstractEncrypter;
import org.simplesocks.netty.common.encrypt.BytesHolder;
import org.simplesocks.netty.common.exception.BaseSystemException;

/**
 * AES 实现类
 *
 * @author zhaohui
 *
 */
public class AesEncrypter extends AbstractEncrypter {

	public final static String CIPHER_AES_128_CFB = "aes-128-cfb";
	public final static String CIPHER_AES_192_CFB = "aes-192-cfb";
	public final static String CIPHER_AES_256_CFB = "aes-256-cfb";
	public final static String CIPHER_AES_128_OFB = "aes-128-ofb";
	public final static String CIPHER_AES_192_OFB = "aes-192-ofb";
	public final static String CIPHER_AES_256_OFB = "aes-256-ofb";
	private final static Map<String, String> ciphers = new HashMap<>();
	static {
		ciphers.put(CIPHER_AES_128_CFB, AesEncrypter.class.getName() );
		ciphers.put(CIPHER_AES_192_CFB, AesEncrypter.class.getName());
		ciphers.put(CIPHER_AES_256_CFB, AesEncrypter.class.getName());
		ciphers.put(CIPHER_AES_128_OFB, AesEncrypter.class.getName());
		ciphers.put(CIPHER_AES_192_OFB, AesEncrypter.class.getName());
		ciphers.put(CIPHER_AES_256_OFB, AesEncrypter.class.getName());
	}


	public static Map<String, String> getCipherImplements() {
		return ciphers;
	}

	public AesEncrypter(String name, String password) {
		super(name, password);
	}

	@Override
	public int getKeyLength() {
		switch (name){
			case CIPHER_AES_128_CFB:
			case CIPHER_AES_128_OFB:
				return 16;
			case CIPHER_AES_192_CFB:
			case CIPHER_AES_192_OFB:
				return 24;
			case CIPHER_AES_256_CFB:
			case CIPHER_AES_256_OFB:
				return 32;
			default:
				throw new IllegalArgumentException("not found "+name);
		}
	}



 	@Override
	protected StreamBlockCipher getCipher0(){
		AESFastEngine engine = new AESFastEngine();
		switch (name){
			case CIPHER_AES_128_CFB:
			case CIPHER_AES_192_CFB:
			case CIPHER_AES_256_CFB:
				return new CFBBlockCipher(engine, getIVLength() * 8);
			case CIPHER_AES_128_OFB:
			case CIPHER_AES_192_OFB:
			case CIPHER_AES_256_OFB:
				return new OFBBlockCipher(engine, getIVLength() * 8);
				default:
					throw new BaseSystemException("Failed to get cipher of"+ name);
		}
	}

	@Override
	public int getIVLength() {
		return 16;
	}

	@Override
	protected SecretKey getKey() {
		return new SecretKeySpec(ssKey.getEncoded(), "AES");
	}


	@Override
	public BytesHolder encrypt(BytesHolder rawBytes) {

		byte[] buffer = new byte[rawBytes.length()];
		encCipher.processBytes(rawBytes.getAllBytes(), 0, rawBytes.length(), buffer,0);
		return new DefaultByteHolder(buffer);
	}

	@Override
	public BytesHolder decrypt(BytesHolder encodedBytes) {
		byte[] buffer = new byte[encodedBytes.length()];
		decCipher.processBytes(encodedBytes.getAllBytes(), 0, encodedBytes.length(), buffer, 0);
		return new DefaultByteHolder(buffer);
	}


	public static void main(String[] args) {
		AesEncrypter aesEncrypter = new AesEncrypter(CIPHER_AES_128_CFB, "34234");

		DefaultByteHolder holder = DefaultByteHolder.valueOf("你好");
		BytesHolder encrypt = aesEncrypter.encrypt(holder);
		BytesHolder decrypt = aesEncrypter.decrypt(encrypt);

		String s = new String(decrypt.getAllBytes(), StandardCharsets.UTF_8);

		System.out.println(".");


	}


}
