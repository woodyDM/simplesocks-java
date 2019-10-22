package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.encrypter.CaesarEncrypter;
import org.simplesocks.netty.common.exception.ProtocolParseException;

import java.nio.charset.StandardCharsets;

/**
 * static factory method
 */
@Slf4j
public class SimpleSocksMessageFactory {

    /**
     * parse message from byteBuf
     *          | cmd | left |
     * length   |  1  | ?   |
     *
     * @param byteBuf
     * @return
     */
    public static SimpleSocksMessage parseMessage(ByteBuf byteBuf){
        byte type = byteBuf.readByte();
        DataType dataType = DataType.parseByte(type);
        switch (dataType){
            case CONNECT:
                return createConnectMessage(byteBuf);
            case CONNECT_RESPONSE:
                return createConnectResponseMessage(byteBuf);
            case PROXY_DATA:
                return createProxyDataMessage(byteBuf);
            case PROXY_DATA_RESPONSE:
                return createProxyDataResponseMessage(byteBuf);
            default:
                throw new IllegalStateException("unable to parse ss data "+ dataType);
        }
    }

    /**
     *
     * @param byteBuf
     * @return
     */
    private static SimpleSocksMessage createProxyDataResponseMessage(ByteBuf byteBuf) {
        byte responseResult = byteBuf.readByte();
        String responseId = parseId(byteBuf);
        if(responseResult== Constants.RESPONSE_FAIL){
            return new ProxyDataResponse(ServerResponseMessage.Code.FAIL,responseId);
        }else if(responseResult==Constants.RESPONSE_SUCCESS){
            return new ProxyDataResponse(ServerResponseMessage.Code.SUCCESS, responseId);
        }else{
            throw new ProtocolParseException("failed to parse response code");
        }
    }

    /**
     *
     * @param byteBuf
     * @return
     */
    private static SimpleSocksMessage createProxyDataMessage(ByteBuf byteBuf) {
        String id = parseId(byteBuf);
        int dataLen = byteBuf.readableBytes();
        byte[] data = new byte[dataLen];
        byteBuf.readBytes(data);
        return new ProxyDataMessage(id, data);
    }

    private static SimpleSocksMessage createConnectResponseMessage(ByteBuf byteBuf) {
        byte result = byteBuf.readByte();
        byte encLen = byteBuf.readByte();
        byte[] enc = new byte[encLen];
        byte encPasswordLen = byteBuf.readByte();
        byte[] encIv = new byte[encPasswordLen];
        byteBuf.readBytes(enc);
        String encType = new String(enc, StandardCharsets.UTF_8);
        byteBuf.readBytes(encIv);
        if(result== Constants.RESPONSE_FAIL){
            return ConnectionResponse.fail(encType);
        }else if(result==Constants.RESPONSE_SUCCESS){
            return new ConnectionResponse(ServerResponseMessage.Code.SUCCESS, encType, encIv);
        }else{
            throw new ProtocolParseException("failed to parse response code");
        }
    }

    /**
     *          |authLength | encTypeLength | authContent   | encType   |
     *          | 1         |   1           | authLen       |  encLen   |
     * @param byteBuf
     * @return
     */
    private static SimpleSocksMessage createConnectMessage(ByteBuf byteBuf) {
        byte authLen = byteBuf.readByte();
        byte encLen = byteBuf.readByte();
        byte[] authBytes = new byte[authLen];
        byteBuf.readBytes(authBytes);
        byte[] encBytes = new byte[encLen];
        byteBuf.readBytes(encBytes);

        byte proxyType = byteBuf.readByte();
        ConnectionMessage.Type proxyTypeEnum = ConnectionMessage.Type.valueOf(proxyType);
        short port = byteBuf.readShort();
        int iPort = port <0 ? (65536 + port) : (int)port;  //fix short overflow

        byte offset = byteBuf.readByte();
        CaesarEncrypter e = new CaesarEncrypter(offset);
        int hostLen = byteBuf.readableBytes();
        byte[] hostBytes = new byte[hostLen];
        byteBuf.readBytes(hostBytes);

        authBytes = e.decrypt(authBytes);
        hostBytes = e.decrypt(hostBytes);
        encBytes = e.decrypt(encBytes);
        String auth = new String(authBytes, StandardCharsets.UTF_8);
        String encType = new String(encBytes, StandardCharsets.UTF_8);
        String host = new String(hostBytes, StandardCharsets.UTF_8);
        return new ConnectionMessage(auth, encType, host, iPort, proxyTypeEnum);
    }

    private static String parseId(ByteBuf byteBuf){
        byte idLen = byteBuf.readByte();
        byte[] idBytes = new byte[idLen];
        byteBuf.readBytes(idBytes);
        return new String(idBytes, StandardCharsets.UTF_8);
    }
}
