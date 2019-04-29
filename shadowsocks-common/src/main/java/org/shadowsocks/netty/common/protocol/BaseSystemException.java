package org.shadowsocks.netty.common.protocol;

public class BaseSystemException extends RuntimeException {

    public BaseSystemException() {
    }

    public BaseSystemException(String message) {
        super(message);
    }

    public BaseSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseSystemException(Throwable cause) {
        super(cause);
    }

    public BaseSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
