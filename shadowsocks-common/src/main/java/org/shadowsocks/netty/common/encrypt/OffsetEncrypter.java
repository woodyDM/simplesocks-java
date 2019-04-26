package org.shadowsocks.netty.common.encrypt;

public class OffsetEncrypter implements Encrypter {


    private byte offset;

    public OffsetEncrypter(byte offset) {
        this.offset = offset;
    }

    @Override
    public byte[] encode(byte[] rawBytes) {
        byte[] result = new byte[rawBytes.length];
        for (int i = 0; i < rawBytes.length; i++) {
            result[i] =(byte)(rawBytes[i]+offset);
        }
        return result;
    }

    @Override
    public byte[] decode(byte[] encodedBytes) {
        byte[] result = new byte[encodedBytes.length];
        for (int i = 0; i < encodedBytes.length; i++) {
            result[i] =(byte)(encodedBytes[i]-offset);
        }
        return result;
    }


}
