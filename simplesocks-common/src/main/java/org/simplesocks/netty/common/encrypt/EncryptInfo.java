package org.simplesocks.netty.common.encrypt;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncryptInfo {

    private String type;
    private byte[] iv;

    public EncryptInfo(String type, byte[] iv) {
        this.type = type;
        this.iv = iv;
    }
}
