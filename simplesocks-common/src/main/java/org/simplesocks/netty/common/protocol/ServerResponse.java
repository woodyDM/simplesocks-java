package org.simplesocks.netty.common.protocol;


public class ServerResponse extends ByteBasedRequest {

    protected Code code;

    public Code getCode() {
        return code;
    }

    public ServerResponse(DataType type, Code code) {
        super(type);
        this.code = code;

    }

    @Override
    protected byte[] body() {
        return new byte[]{code.bit};
    }

    public enum Code{

        SUCCESS(Constants.RESPONSE_SUCCESS),
        FAIL(Constants.RESPONSE_FAIL);
        byte bit;

        Code(int bit) {
            this.bit = (byte)bit;
        }
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                 type + ':'+code+
                '}';
    }
}
