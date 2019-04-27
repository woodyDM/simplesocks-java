package org.shadowsocks.netty.common.protocol;

public class ProtocolParseException extends RuntimeException {

    public ProtocolParseException() {
    }

    public ProtocolParseException(String message) {
        super(message);
    }

    public ProtocolParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolParseException(Throwable cause) {
        super(cause);
    }

    public ProtocolParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
