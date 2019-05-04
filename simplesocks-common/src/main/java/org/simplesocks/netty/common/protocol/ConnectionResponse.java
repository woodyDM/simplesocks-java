package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.simplesocks.netty.common.util.ContentUtils;

import java.nio.charset.StandardCharsets;

@Getter
public class ConnectionResponse extends ServerResponseMessage {

    private String encType;
    private String encPassword;

    public ConnectionResponse(Code code, String encType, String encPassword) {
        super(DataType.CONNECT_RESPONSE, code);
        this.encType = encType;
        this.encPassword = encPassword;
    }

    @Override
    protected byte[][] body() {
        byte[] p1 = new byte[3];
        byte[] p2 = encType.getBytes(StandardCharsets.UTF_8);
        byte[] p3 = encPassword.getBytes(StandardCharsets.UTF_8);
        p1[0] = code.bit;
        p1[1] = (byte)p2.length;
        p1[2] = (byte)p3.length;
        ContentUtils.checkBitLength("encType", p2.length);
        ContentUtils.checkBitLength("encPassword",p3.length);
        byte[][] result = new byte[3][];
        result[0] = p1;
        result[1] = p2;
        result[2] = p3;
        return result;
    }

    public static void main(String[] args) {
        ConnectionResponse request = new ConnectionResponse(Code.SUCCESS,"1234😊笑", "shadow大大大efw");
        byte[][] body = request.body();
        byte[] header = new byte[1];
        header[0] = (byte)0x11;
        ByteBuf byteBuf = Unpooled.wrappedBuffer(header, body[0],body[1],body[2]  );
        SimpleSocksMessage simpleSocksMessage = SimpleSocksMessageFactory.newInstance(byteBuf);

        System.out.println(".");
    }

    @Override
    public String toString() {
        return "ConnectionResponse{" +
                "type='" + encType + '\'' +
                ", encPassword='" + encPassword + '\'' +
                "," + code +
                "," + type +
                '}';
    }
}
