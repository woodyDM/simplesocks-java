package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.util.ContentUtils;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

@Getter
@Slf4j
public class ConnectionMessage extends ByteBasedMessage {

    private String auth ;
    private String encryptType;
    private String host;
    private int port;
    private Type hostType;

    public ConnectionMessage(String auth, String encryptType, String host, int port, Type hostType) {
        super(DataType.CONNECT);
        if(auth==null||auth.length()==0){
            throw new IllegalArgumentException("Must provide auth!");
        }
        Objects.requireNonNull(encryptType);
        Objects.requireNonNull(host);
        Objects.requireNonNull(hostType);
        this.auth = auth;
        this.encryptType = encryptType;
        this.host = host;
        this.port = port;
        this.hostType = hostType;
    }



    public String getAuth() {
        return auth;
    }

    @Override
    protected byte[][] body() {

        byte[] authBytes = auth.getBytes(StandardCharsets.UTF_8);
        byte[] encryptTypeBytes = encryptType.getBytes(StandardCharsets.UTF_8);
        byte[] hostBytes = host.getBytes(StandardCharsets.UTF_8);
        byte[] p1 = new byte[2];
        ContentUtils.checkBitLength("auth", authBytes.length);
        ContentUtils.checkBitLength("enctype", encryptTypeBytes.length);
        p1[0] = (byte)authBytes.length;
        p1[1] = (byte)encryptTypeBytes.length;
        byte[] p4 = new byte[4];
        p4[0] = hostType.getBit();
        byte[] portBytes = ContentUtils.shortToByte((short) port);
        p4[1] = portBytes[0];
        p4[2] = portBytes[1];
        Random random = new Random();
        int i = 128 - random.nextInt(256);
        byte offset = (byte) i;
        p4[3] = offset;
        OffsetEncrypter encrypter = new OffsetEncrypter(offset);
        hostBytes = encrypter.encode(hostBytes);
        authBytes = encrypter.encode(authBytes);
        encryptTypeBytes = encrypter.encode(encryptTypeBytes);
        byte[][] result =  new byte[5][];
        result[0]=p1;
        result[1]=authBytes;
        result[2]=encryptTypeBytes;
        result[3]=p4;
        result[4]=hostBytes;
        return result;
    }


    public static void main(String[] args) {
        ConnectionMessage request = new ConnectionMessage("1234😊笑", "shadow大大大efw", "www.google.com9080哈哈", 18974, Type.DOMAIN);
        byte[][] body = request.body();
        byte[] header = new byte[1];
        header[0] = (byte)0x01;
        ByteBuf byteBuf = Unpooled.wrappedBuffer(header, body[0],body[1],body[2],body[3],body[4] );
        SimpleSocksMessage simpleSocksMessage = SimpleSocksMessageFactory.newInstance(byteBuf);

        System.out.println(".");
    }

    @Override
    public String toString() {
        return "ConnectionMessage{" +
                "host="+host+":"+port+"  en="+encryptType;
    }

    @Getter
    public enum Type{
        IPV4(Constants.PROXY_TYPE_IPV4),
        DOMAIN(Constants.PROXY_TYPE_DOMAIN),
        IPV6(Constants.PROXY_TYPE_IPV6);

        byte bit;

        Type(int bit) {
            this.bit = (byte)bit;
        }

        public static Type valueOf(byte b){
            for(Type it:values()){
                if(it.bit==b)return it;
            }
            throw new NoSuchElementException("no Type found for "+b);
        }

    }
}
