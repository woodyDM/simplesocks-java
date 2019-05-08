package org.simplesocks.netty.common.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import javax.crypto.SecretKey;
import java.security.SecureRandom;

@Slf4j
public abstract class AbstractEncrypter   {

	protected final String name;
	protected final SecretKey key;
	protected final SimpleSocksKey ssKey;
	protected final int ivLength;
	protected final int keyLength;
	protected StreamBlockCipher encCipher;
	protected StreamBlockCipher decCipher;

	public AbstractEncrypter(String name, String password) {
		this.name = name.toLowerCase();
		ivLength = getIVLength();
		keyLength = getKeyLength();
		ssKey = new SimpleSocksKey(password, keyLength);
		key = getKey();
		init();
	}


	private void init(){
		this.encCipher = randomCipher(true);
		this.decCipher = randomCipher(false);
	}

	private StreamBlockCipher randomCipher(boolean isEncrypt){
		byte[] randomBytes = new byte[ivLength];
		new SecureRandom().nextBytes(randomBytes);
		StreamBlockCipher cipher = getCipher0();
		ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(key.getEncoded()), randomBytes);
		cipher.init(isEncrypt, parameterIV);
		return cipher;
	}


	public abstract BytesHolder encrypt(BytesHolder rawBytes);

	public abstract BytesHolder decrypt(BytesHolder encodedBytes) ;


	protected abstract StreamBlockCipher getCipher0();

	protected abstract SecretKey getKey();

	protected abstract int getIVLength();

	protected abstract int getKeyLength();
}
