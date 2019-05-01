package org.simplesocks.netty.common.protocol;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.util.ContentUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CmdRequestFactory {


    public static SimpleSocksCmdRequest newInstance(ByteBuf byteBuf){

        byte type = byteBuf.readByte();
        DataType dataType = DataType.parseByte(type);
        switch (dataType){
            case CONNECT:
            {
                byte auth = byteBuf.readByte();
                if(auth==Constants.NO_AUTH){
                    return new NoAuthConnectionRequest();
                }else if(auth==Constants.AUTH){
                    String authStr = ContentUtils.leftBytesToString(byteBuf);
                    return new AuthConnectionRequest(authStr);
                }else{
                    throw new ProtocolParseException("unable to parse auth type."+auth);
                }
            }
            case PROXY:{
                byte proxyType = byteBuf.readByte();
                ProxyRequest.Type proxyTypeEnum = ProxyRequest.Type.valueOf(proxyType);
                short i = byteBuf.readShort();
                byte offset = byteBuf.readByte();
                int len = byteBuf.readableBytes();
                byte[] tar = new byte[len];
                byteBuf.readBytes(tar);
                OffsetEncrypter e = new OffsetEncrypter(offset);
                byte[] decode = e.decode(tar);
                String target = new String(decode, StandardCharsets.UTF_8);
                return new ProxyRequest(proxyTypeEnum, (int)i, target);
            }
            case END_PROXY:{
                return new EndProxyRequest();
            }
            case PROXY_DATA:{
                int len = byteBuf.readableBytes();
                byte[] data = new byte[len];
                byteBuf.readBytes(data);
                return new ProxyDataRequest(data);
            }
            case PING:{
                return PingRequest.getInstance();
            }
            case PONG:{
                return PongRequest.getInstance();
            }
            case END_CONNECTION:{
                return EndConnectionRequest.getInstance();
            }
            case CONNECT_RESPONSE:
            case PROXY_RESPONSE:
            case PROXY_DATA_RESPONSE:
            case END_PROXY_RESPONSE:{
                byte code = byteBuf.readByte();
                if(code==Constants.RESPONSE_SUCCESS){
                    return new ServerResponse(dataType, ServerResponse.Code.SUCCESS);
                }else if(code==Constants.RESPONSE_FAIL){
                    return new ServerResponse(dataType, ServerResponse.Code.FAIL);
                }else{
                    throw new ProtocolParseException("unable to parse response code"+code);
                }
            }
            default:
                throw new IllegalStateException("unable to parse ss data "+ dataType);
        }
    }
}
