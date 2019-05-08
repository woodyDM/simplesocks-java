package org.simplesocks.netty.common.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.exception.BaseSystemException;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 *
 */
@Slf4j
public class SimpleSocksKey implements SecretKey {


	private final static int KEY_LENGTH = 32;
	private byte[] key;
	private int length;

	public SimpleSocksKey(String password) {
		length = KEY_LENGTH;
		key = init(password);
	}

	public SimpleSocksKey(String password, int length) {
		this.length = length;
		key = init(password);
	}

	private byte[] init(String password) {
		MessageDigest md;
		byte[] keys = new byte[KEY_LENGTH];
		byte[] temp = null;
		byte[] hash = null;
		byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

		try {
			md = MessageDigest.getInstance("MD5");
		}catch (Exception e) {
            log.error("Secrete key init error", e);
			throw new BaseSystemException("Failed sec init.",e);
		}

        int i = 0;
		while (i < keys.length) {
			if (i == 0) {
				hash = md.digest(passwordBytes);
				temp = new byte[passwordBytes.length + hash.length];
			} else {
				System.arraycopy(hash, 0, temp, 0, hash.length);
				System.arraycopy(passwordBytes, 0, temp, hash.length, passwordBytes.length);
				hash = md.digest(temp);
			}
			System.arraycopy(hash, 0, keys, i, hash.length);
			i += hash.length;
		}

		if (length < KEY_LENGTH) {
			byte[] keysl = new byte[length];
			System.arraycopy(keys, 0, keysl, 0, length);
			return keysl;
		}else{
            return keys;
        }
	}

	@Override
	public String getAlgorithm() {
		return "simpleSocks";
	}

	@Override
	public String getFormat() {
		return "RAW";
	}

	@Override
	public byte[] getEncoded() {
		return key;
	}
}
