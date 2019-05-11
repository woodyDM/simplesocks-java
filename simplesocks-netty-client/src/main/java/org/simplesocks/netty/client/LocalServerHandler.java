package org.simplesocks.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.encrypt.EncryptInfo;
import org.simplesocks.netty.common.protocol.*;
import org.simplesocks.netty.common.util.ServerUtils;

import java.util.Arrays;


@Slf4j
public class LocalServerHandler extends SimpleChannelInboundHandler<SimpleSocksMessage> {


    private SimpleSocksProtocolClient client;

    public LocalServerHandler(SimpleSocksProtocolClient client) {
        this.client = client;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksMessage msg) throws Exception {
        DataType type = msg.getType();
        switch (type){
            case CONNECT_RESPONSE:{
                ConnectionResponse response = (ConnectionResponse)msg;
                if(response.getCode()== ServerResponseMessage.Code.SUCCESS){
                    EncryptInfo info = new EncryptInfo(response.getEncType(), response.getEncIV());
                    log.debug("Connect ok ,channel {}, iv {}",ctx.channel().localAddress(), Arrays.toString(info.getIv()));
                    client.setEncInfo(info);
                    client.setConnected(true);
                    client.getConnectionPromise().setSuccess(ctx.channel());
                }else{
                    log.debug("Connection auth failed , set promise fail.");
                    client.getConnectionPromise().setFailure(new BaseSystemException("Failed to auth."));
                }
                break;
            }
            case PROXY_DATA:{
                ProxyDataMessage request = (ProxyDataMessage)msg;
                EncryptInfo info = client.getEncInfo();
                Encrypter encrypter = client.getEncrypterFactory().newInstant(info.getType(), info.getIv());
                byte[] encrypted = request.getData();
                byte[] plain = encrypter.decrypt(encrypted);
                client.onReceiveProxyData(new ProxyDataMessage(request.getId(), plain));
                break;
            }
            case PROXY_DATA_RESPONSE:{
                ProxyDataResponse response = (ProxyDataResponse)msg;
                if(response.getCode() == ServerResponseMessage.Code.FAIL){
                    client.close();
                }
                break;
            }
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ServerUtils.logException(log, cause);
        close(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        close(ctx);
    }

    private void close(ChannelHandlerContext ctx ){
        client.close();
    }

}
