package org.shadowsocks.netty.common.encrypt;

public interface Encrypter {


    byte[] encode(byte[] rawBytes);

    byte[] decode(byte[] encodedBytes);


}
