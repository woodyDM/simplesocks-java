package org.simplesocks.netty.common.encrypter

import org.simplesocks.netty.common.encrypt.EncType
import org.simplesocks.netty.common.encrypt.encrypter.CaesarEncrypter
import org.simplesocks.netty.common.encrypt.factory.CaesarFactory
import spock.lang.Specification

class CaesarFactorySpec extends  Specification{

    CaesarFactory factory

    def setup(){
        factory = new CaesarFactory()
    }

    def "should support caesar"(){
        when:"given a valid value"
        then:
        factory.support(EncType.CAESAR.encName)
    }

    def "should not support aes"(){
        when:"given a aes value"
        then:
        !factory.support(EncType.AES_CBC.encName)
    }

    def "should only generate iv length of 1"(){
        when:"call random iv"
        then:
        factory.randomIv(null).length == 1
    }

    def "should create new  caesar instance"(){
        when:"call newInstance"
        def ins =factory.newInstant(EncType.CAESAR.encName, [1] as byte[])
        then:
        ins != null
        then:
        def encrpt = (CaesarEncrypter) ins
        encrpt.getOffset() == (byte) 1
    }
}
