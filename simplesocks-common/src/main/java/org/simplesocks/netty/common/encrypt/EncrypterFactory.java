package org.simplesocks.netty.common.encrypt;



public interface EncrypterFactory {

    /**
     * set appKey for factory
     * @param appKey
     */
    void registerKey(byte[] appKey);

    /**
     * does this manager support the encType
     * @param encType
     * @return
     */
    boolean support(String encType);

    /**
     * create new encrypter for encType whith iv.
     * @param encType
     * @param iv
     * @return
     */
    Encrypter newInstant(String encType, byte[] iv);

    /**
     * generate random iv for the encType
     * @param encType
     * @return
     */
    byte[] randomIv(String encType);

}
