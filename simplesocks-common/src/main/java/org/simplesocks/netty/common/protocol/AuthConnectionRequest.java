package org.simplesocks.netty.common.protocol;

import java.nio.charset.StandardCharsets;

public class AuthConnectionRequest extends ByteBasedRequest {

    private String auth ;

    public AuthConnectionRequest(String auth) {
        super(DataType.CONNECT);
        if(auth==null||auth.length()==0){
            throw new IllegalArgumentException("must provide auth");
        }
        this.auth = auth;
    }

    public String getAuth() {
        return auth;
    }

    @Override
    protected byte[] body() {
        byte[] strBytes = auth.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[strBytes.length+1];
        bytes[0] = Constants.AUTH;
        System.arraycopy(strBytes,0,bytes,1,strBytes.length);
        return bytes;
    }

    @Override
    public String toString() {
        return "AuthConnectionRequest{" +
                "auth='" + auth +"}";
    }
}
