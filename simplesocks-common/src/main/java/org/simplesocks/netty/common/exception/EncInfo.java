package org.simplesocks.netty.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncInfo {
    private String type;
    private byte[] iv;

    public EncInfo(String type, byte[] iv) {
        this.type = type;
        this.iv = iv;
    }
}
