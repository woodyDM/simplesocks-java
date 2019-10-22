package org.simplesocks.netty.common.encrypt;

import lombok.Getter;

/**
 * all type this program supports
 */
@Getter
public enum EncType {

    CAESAR("caesar"),
    AES_CBC( "aes-cbc"),
    AES_CFB("aes-cfb");

    private String encName;

    EncType(String encName) {
        this.encName = encName;
    }
}
