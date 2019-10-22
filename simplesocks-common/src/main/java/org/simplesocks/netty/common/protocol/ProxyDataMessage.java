package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Getter
public class ProxyDataMessage extends ByteBasedMessage {


    private byte[] data;
    private String id ;

    public ProxyDataMessage( byte[] data) {
        super(DataType.PROXY_DATA);
        this.data = data;
        this.id = UUID.randomUUID().toString().replace("-","");
    }

    public ProxyDataMessage(String id, byte[] data) {
        super(DataType.PROXY_DATA);
        this.data = data;
        this.id = id;
    }

    @Override
    protected byte[][] body() {

        byte[] idBytes = id.getBytes(StandardCharsets.UTF_8);
        byte len = (byte)idBytes.length;
        byte[] p1 = new byte[1];
        p1[0] = len;
        byte[][] result = new byte[3][];
        result[0] = p1;
        result[1] = idBytes;
        result[2] = data;
        return result;
    }

    public static void main(String[] args) {
        String s = new String("哈哈2342.flkj=-");
        ProxyDataMessage proxyDataMessage = new ProxyDataMessage(s.getBytes(StandardCharsets.UTF_8));

        byte[][] body = proxyDataMessage.body();
        byte[] header = new byte[1];
        header[0] = (byte)0x02;
        ByteBuf byteBuf = Unpooled.wrappedBuffer(header, body[0],body[1],body[2]  );
        SimpleSocksMessage simpleSocksMessage = SimpleSocksMessageFactory.parseMessage(byteBuf);
        ProxyDataMessage proxyDataMessage2 = (ProxyDataMessage)simpleSocksMessage;
        String s1 = new String(proxyDataMessage2.data, StandardCharsets.UTF_8);
        System.out.println(".");
    }

    @Override
    public String toString() {
        return "ProxyDataMessage{" +
                "dataLen = " + data.length +
                ", id='" + id + '\'' +
                '}';
    }
}
