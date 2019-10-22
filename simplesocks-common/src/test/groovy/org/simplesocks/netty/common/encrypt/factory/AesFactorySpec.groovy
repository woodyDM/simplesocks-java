package org.simplesocks.netty.common.encrypt.factory

import org.simplesocks.netty.common.encrypt.EncType
import org.simplesocks.netty.common.encrypt.encrypter.AesEncrypter
import spock.lang.Specification

class AesFactorySpec extends Specification{

    AesFactory factory;

    def setup(){
        factory = new AesFactory()
    }


    def "should support aes of two type"(){
        expect:
        result == factory.support(t)
        where:
        result  |   t
        true    |   EncType.AES_CBC.encName
        true    |   EncType.AES_CFB.encName
        false   |   EncType.CAESAR.encName
    }


    def "should init aes encrypter"(){
        given:
        def type = EncType.AES_CBC.encName
        def appkey = [1,2,3,4,5] as byte[]
        factory.registerKey(appkey)
        def iv = factory.randomIv(type)
        when:
        def ins = factory.newInstant(type, iv)
        then:
        ins instanceof  AesEncrypter
        then:
        AesEncrypter enc = (AesEncrypter) ins
        expect:
        enc.appKey[i] == b
        where:
        i   | b
        0   | (byte)1
        2   | (byte)3


    }

    def "should padding < 16 length"(){
        given:
        def appkey = [1,2,3,4,5] as byte[]
        when:
        factory.registerKey(appkey)
        then:
        factory.appKey.length == 16
        factory.appKey[1] == (byte)2
        factory.appKey[4] == (byte)5
        factory.appKey[15] == (byte)0
    }

    def "should padding < 24 length"(){
        given:
        def appkey = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18] as byte[]
        when:
        factory.registerKey(appkey)
        then:
        factory.appKey.length == 24
        factory.appKey[1] == (byte)2
        factory.appKey[4] == (byte)5
        factory.appKey[15] == (byte)16
        factory.appKey[20] ==(byte)0
    }

    def "should padding < 32 length"(){
        given:
        def appkey = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27] as byte[]
        when:
        factory.registerKey(appkey)
        then:
        factory.appKey.length == 32
        factory.appKey[1] == (byte)2
        factory.appKey[4] == (byte)5
        factory.appKey[15] == (byte)16
        factory.appKey[20] ==(byte)21
        factory.appKey[31] == (byte)0
    }
}
