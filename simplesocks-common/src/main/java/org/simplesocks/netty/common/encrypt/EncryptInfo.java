package org.simplesocks.netty.common.encrypt;

import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
public class EncryptInfo {

    /**
     * encType
     */
    private String type;
    /**
     * iv of encType
     */
    private byte[] iv;

    public EncryptInfo(String type, byte[] iv) {
        this.type = type;
        this.iv = iv;
    }
}
