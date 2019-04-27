package org.shadowsocks.netty.common.protocol;

import io.netty.util.internal.StringUtil;
import org.shadowsocks.netty.common.encrypt.OffsetEncrypter;
import org.shadowsocks.netty.common.util.ContentUtils;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ProxyRequest extends ByteBasedRequest {

    private int port;
    private String target;
    private Type proxyType;

    public ProxyRequest(Type t, int port, String target) {
        super(DataType.PROXY);
        if(StringUtil.isNullOrEmpty(target)){
            throw new IllegalArgumentException("target empty!");
        }
        this.proxyType = t;
        this.port = port;
        this.target=target;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    public Type getProxyType() {
        return proxyType;
    }

    @Override
    protected byte[] body() {
        byte[] str = target.getBytes(StandardCharsets.UTF_8);
        int len = str.length + Constants.LEN_PROXY;
        byte[] portBytes = ContentUtils.shortToByte((short) port);
        byte[] bytes = new byte[len];
        bytes[0] = proxyType.bit;
        bytes[1] = portBytes[0];
        bytes[2] = portBytes[1];
        Random random = new Random();
        int i = 128 - random.nextInt(256);
        byte offset = (byte) i;
        bytes[3] = offset;
        OffsetEncrypter encrypter = new OffsetEncrypter(offset);
        str = encrypter.encode(str);
        System.arraycopy(str,0,bytes,4, str.length);
        return bytes;
    }


    @Override
    public String toString() {
        return "ProxyRequest{" +
                "port=" + port +
                ", target='" + target + '\'' +
                ", proxyType=" + proxyType +
                '}';
    }

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
            return null;
        }

    }
}
