package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.simplesocks.netty.common.util.ContentUtils;

import java.nio.charset.StandardCharsets;

@Getter
public class ConnectionResponse extends ServerResponseMessage {

    private String encType;
    private byte[] encIV;

    public ConnectionResponse(Code code, String encType,  byte[] encIV) {
        super(DataType.CONNECT_RESPONSE, code);
        this.encType = encType;
        this.encIV = encIV;
    }

    public static ConnectionResponse fail(String encType){
        return new ConnectionResponse(ServerResponseMessage.Code.FAIL, encType,new byte[1]);
    }

    @Override
    protected byte[][] body() {
        byte[] p1 = new byte[3];
        byte[] p2 = encType.getBytes(StandardCharsets.UTF_8);
        byte[] p3 = encIV;
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



    @Override
    public String toString() {
        return "ConnectionResponse{" +
                "type='" + encType + '\'' +
                ", len='" + encIV.length + '\'' +
                "," + code +
                "," + type +
                '}';
    }
}
