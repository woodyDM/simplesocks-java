package org.simplesocks.netty.common.encrypt.encrypter

import org.simplesocks.netty.common.encrypt.encrypter.AesEncrypter
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class AesEncrypterSpec extends Specification{

    AesEncrypter encrypterCFB;
    AesEncrypter encrypterCBC
    byte [] appKey
    byte [] iv
    def setup(){
        appKey = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16] as byte[]
        iv = [2,3,41,3,13,3,12,-2,-13,-123,23,2,123,4,45,32] as  byte[]
        encrypterCBC = new AesEncrypter('cbc', appKey, iv)
        encrypterCFB = new AesEncrypter('cfb',appKey, iv)
    }

    def "should enc and dec for cbc"(){
        given: "a raw string"
        def raw= "jiami加密🔐"
        when: "enc and dec "
        def result = encrypterCBC.encrypt(raw.getBytes(StandardCharsets.UTF_8))
        def dec = encrypterCBC.decrypt(result)
        def decString = new String(dec, StandardCharsets.UTF_8)
        then: "should eq"
        decString == raw
    }

    def "should enc and dec for cfb"(){
        given:
        def raw= "jiami加密🔐"
        when:
        def result = encrypterCFB.encrypt(raw.getBytes(StandardCharsets.UTF_8))
        def dec = encrypterCFB.decrypt(result)
        def decString = new String(dec, StandardCharsets.UTF_8)
        then:
        decString == raw
    }
}
