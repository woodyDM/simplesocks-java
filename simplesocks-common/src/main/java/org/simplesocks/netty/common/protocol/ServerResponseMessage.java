package org.simplesocks.netty.common.protocol;


public abstract class ServerResponseMessage extends ByteBasedMessage {

    protected Code code;

    public Code getCode() {
        return code;
    }

    public ServerResponseMessage(DataType type, Code code) {
        super(type);
        this.code = code;
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
        return "ServerResponseMessage{" +
                 type + ':'+code+
                '}';
    }
}
