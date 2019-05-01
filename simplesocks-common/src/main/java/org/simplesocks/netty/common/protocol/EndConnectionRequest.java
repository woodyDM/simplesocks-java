package org.simplesocks.netty.common.protocol;

public class EndConnectionRequest extends ByteBasedRequest {

    private static EndConnectionRequest instance = new EndConnectionRequest();

    public static EndConnectionRequest getInstance(){
        return instance;
    }

    private EndConnectionRequest( ) {
        super(DataType.END_CONNECTION);
    }

    @Override
    protected byte[] body() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "EndConnectionRequest{}";
    }
}
