package org.simplesocks.netty.common.encrypt.encrypter


import org.simplesocks.netty.common.encrypt.encrypter.CaesarEncrypter
import spock.lang.Specification

class CaesarEncrypterSpec extends Specification {

    CaesarEncrypter encrypter;
    byte offset;

    def setup() {
        offset = (byte)1
        encrypter = new CaesarEncrypter(offset)
    }

    def "should enc even overflow"(){
        given:"a byte array"
        when:
        def raw = [1,3,4,5, Byte.MAX_VALUE] as byte[]
        def result = encrypter.encrypt(raw)
        then:
        result[0] == (byte)(raw[0] + 1)
        result[1] == (byte)(raw[1] + 1)
        result[2] == (byte)(raw[2] + 1)
        result[3] == (byte)(raw[3] + 1)
        result[4] == Byte.MIN_VALUE
    }

    def "should dec even overflow"(){
        given:"a byte array to dec"
        when:
        def raw = [Byte.MIN_VALUE,3,4,5, Byte.MAX_VALUE] as byte[]
        def result = encrypter.decrypt(raw)
        then:
        result[0] == (byte)(raw[0] - 1)
        result[1] == (byte)(raw[1] - 1)
        result[2] == (byte)(raw[2] - 1)
        result[3] == (byte)(raw[3] - 1)
        result[0] == Byte.MAX_VALUE
    }



}
