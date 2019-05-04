package org.simplesocks.netty.common.exception;

public class ProtocolParseException extends BaseSystemException {

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
