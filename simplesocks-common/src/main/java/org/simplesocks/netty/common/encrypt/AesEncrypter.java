package org.simplesocks.netty.common.encrypt;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.simplesocks.netty.common.encrypt.factory.AesFactory;
import org.simplesocks.netty.common.encrypt.factory.CompositeFactory;
import org.simplesocks.netty.common.encrypt.factory.EncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class AesEncrypter implements Encrypter {

    private Type type;
    private byte[] appKey ;
    private byte[] iv;
    private static final int VECTOR_SIZE = 16;


    public AesEncrypter(String encType, byte[] appKey, byte[] iv) {
        if(iv.length!=VECTOR_SIZE){
            throw new IllegalArgumentException("iv length should be 16");
        }
        this.type = Type.of(encType);
        this.appKey = appKey;
        this.iv = iv;
    }

//
//    /**
//     * CBC,
//     * CFB,
//     *
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception{
//        EncrypterFactory factory = new CompositeFactory();
//        factory.registerKey("yCastle: AES你好啊😊，啦啦BouncEngine,".getBytes());
//        byte[] ivBytes = new byte[16];
//        Random rd = new Random();
//        rd.nextBytes(ivBytes);
//        for (int i = 0; i < 1000; i++) {
//
//
//
//            Encrypter encrypter = factory.newInstant("offset1", ivBytes);
//
//            String content="你好啊😊，啦啦啦（）*&*…...BouncyCastle: AESEngine, AESFastEngine OR AESLightEngine? ...\n" +
//                    "2017年3月28日 - 解决c# - AES encryption with BouncyCastle: AESEngine, AESFastEngine OR AESLightEngine?itPublisher 分享于 2017-03-282019阿里云全部产品优惠券(...\n" +
//                    "www.itkeyword.com/doc/...  - 百度快照\n" +
//                    "AESLightEngine\n" +
//                    "org.bouncycastle.crypto.engines Class AESLig到百度首页\n" +
//                    "org.bouncycastle AESLightEngine thread safe\n" +
//                    "百度首页消息设置woody_ME\n" +
//                    "网页资讯视频图片知道文库贴吧采购地图更多»\n" +
//                    "换一换 搜索热点\n" +
//
//                    "您可以仅查看：英文结果\n" +
//                    "...DES and SALSA20 by Java Based Bouncy Castle ..._百度学术\n" +
//                    "Madhumita Panda , Atul Nag - Second International Conference on Advances in Computing & Communication Engineering - 2015 - 被引量:3\n" +
//                    "Information Security has become an important element of data communication. Various encryption algorithms have been proposed and implemented as a solution ...\n" +
//                    "\n" +
//                    "xueshu.baidu.com \n" +
//                    "...encryption algorithm using Bouncy Castle ..._百度学术\n" +
//                    "M.N. Sivasankari , S. Sujatha - International Conference on Emerging Trends in Electrical & Computer Technology - 2011\n" +
//                    "Mobile Computing devices are increasing enormously in all fields of communication, which aims to focus on secure transfer of information. In order to provi...\n" +
//                    "\n" +
//                    "xueshu.baidu.com \n" +
//                    "...J2ME DENGAN BANTUAN BOUNCY CASTLE CRYPTOGRAPHY ...\n" +
//                    "查看此网页的中文翻译，请点击 翻译此页\n" +
//                    "AES algorithm and with the help of Bouncy Castle Cryptography APIs.The ... so it can be a safe alternative communications for sending short messages...\n" +
//
//                    "3\n" +
//                    "4\n" +
//                    "5\n" +
//                    "6\n" +
//                    "7\n" +
//                    "8\n" +
//                    "9\n" +
//                    "10\n" +
//                    "下一页>\n" +
//                    "帮助举报用户反馈htEngine java.lang.Object org.bouncycastle.crypto.engines.AESLightEngine All Implemented Interfaces: BlockCipher ...\n" +
//                    "www.eecs.berkeley.edu/...  - 百度快照 - 翻译此页\n" +
//                    "...BouncyCastle: AESEngine, AESFastEngine OR AESLightEngine? ...\n" +
//                    "is the difference between AESEngine and AESFastEngine and AESLightEngine? ... AES.ECB OR AES.OFB http://www.bouncycastle.org/docs/docs1.6/index...\n" +
//                    "https://stackoverflow.com/ques...  - 百度快照 - 翻译此页\n" +
//                    "Java Code Examples org.bouncycastle.crypto.engines.DESedeEngine\n" +
//                    "2015年11月1日 - This page provides Java code examples for org.bouncycastle.crypto.engines.DESedeEngine. The examples are extracted from open source Java pro...\n" +
//                    "https://www.programcreek.com/j...  - 百度快照 - 翻译此页\n" +
//                    "Java 运用 Bouncy Castle 进行 AES128 加密解密(CBC 模式 PKCS7 ...\n" +
//                    "2017年6月21日 -  这是它的官网 : http://www.bouncycastle.org/  首页上写着: Here ... cipher = new PaddedBlockCipher( new CBCBlockCipher( new AESLightEngine(...\n" +
//                    "CSDN博客号 - 百度快照\n" +
//                    "org.bouncycastle.crypto.engines.DESEngine Example…%！！~~~1234567";
//
//            byte[] dec = encrypter.encrypt(content.getBytes(StandardCharsets.UTF_8));
//            byte[] decode = encrypter.decrypt(dec);
//            String a = new String(decode, StandardCharsets.UTF_8);
//            if(!a.equals(content)){
//                throw new IllegalStateException(".");
//            }
//
//        }
//        System.out.println("ok");
//
//    }

    @Override
    public byte[] encrypt(byte[] plain) {
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(type.cipher(new AESLightEngine() ));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(appKey),
                iv);
        aes.init(true, ivAndKey);
        return cipherData(aes, plain);
    }

    @Override
    public byte[] decrypt(byte[] encodedBytes) {
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(type.cipher(new AESLightEngine() ));
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(appKey),
                iv);
        aes.init(false, ivAndKey);
        return cipherData(aes, encodedBytes);
    }




    private static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data)  {
        try{
            int minSize = cipher.getOutputSize(data.length);
            byte[] outBuf = new byte[minSize];
            int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
            int length2 = cipher.doFinal(outBuf, length1);
            int actualLength = length1 + length2;
            byte[] result = new byte[actualLength];
            System.arraycopy(outBuf, 0, result, 0, result.length);
            return result;
        } catch (InvalidCipherTextException e) {
            throw new BaseSystemException("Fail in encrypter.",e);
        }

    }


    public enum Type{
        CBC,
        CFB;


        public static Type of(String encType){
            for(Type it:values()){
                if(encType.contains(it.name().toLowerCase())){
                    return it;
                }
            }
            throw new IllegalArgumentException("no type found for "+encType);
        }

        public BlockCipher cipher(BlockCipher engine ){
            switch (this){
                case CBC:
                    return new CBCBlockCipher(engine );
                case CFB:
                    return new CBCBlockCipher(engine );
                    default:
                        throw new IllegalStateException("Type no cipher");
            }
        }
    }
}
