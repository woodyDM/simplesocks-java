package org.simplesocks.netty.common.netty;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.simplesocks.netty.common.protocol.Constants;

public class SimpleSocksDecoder {

    public static LengthFieldBasedFrameDecoder newLengthDecoder(){
        return new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                Constants.LEN_VERSION,
                Constants.LEN_CONTENT_LENGTH,
                -(Constants.LEN_VERSION + Constants.LEN_CONTENT_LENGTH),
                Constants.LEN_VERSION + Constants.LEN_CONTENT_LENGTH);
    }

}
