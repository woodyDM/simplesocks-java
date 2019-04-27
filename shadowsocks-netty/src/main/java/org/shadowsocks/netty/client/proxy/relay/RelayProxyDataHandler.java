package org.shadowsocks.netty.client.proxy.relay;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.client.proxy.LocalDataRelayHandler;
import org.shadowsocks.netty.client.proxy.RemoteDataRelayHandler;
import org.shadowsocks.netty.client.proxy.ServerConnectToRemoteHandler;
import org.shadowsocks.netty.common.protocol.*;

@Slf4j
public class RelayProxyDataHandler extends SimpleChannelInboundHandler<SimpleSocksCmdRequest> {

    private String targetHost;
    private int port;
    private SocksAddressType socksAddressType;
    private Channel remoteChannel;
    private Channel localChannel;

    public RelayProxyDataHandler(Channel localChannel,Channel remoteChannel,SocksAddressType socksAddressType ) {
        this.localChannel = localChannel;
        this.remoteChannel = remoteChannel;
        this.socksAddressType = socksAddressType;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SimpleSocksCmdRequest simpleSocksCmdRequest) throws Exception {
        DataType type = simpleSocksCmdRequest.getType();
        switch (type){
            case PROXY_DATA:{
                if(localChannel.isActive()){
                    ProxyDataRequest request = (ProxyDataRequest)simpleSocksCmdRequest;
                    int len = request.getIncomingBuf().readableBytes();
                    byte[] bytes = new byte[len];
                    request.getIncomingBuf().readBytes(bytes);
                    localChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
                    log.debug("send to local channel len {}",len);
                    //send to local
                }
                break;
            }
            case PROXY_DATA_RESPONSE:{
                log.info("proxy data response! {}",simpleSocksCmdRequest);
                ServerResponse response = (ServerResponse)simpleSocksCmdRequest;
                if(response.getCode()== ServerResponse.Code.SUCCESS){
                    log.debug("server already receive data");
                }else{
                    channelHandlerContext.channel().close();
                }
                break;
            }
        }


    }


    private SocksCmdResponse getSuccessResponse(SocksCmdRequest request) {
        return new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4);
    }

    private SocksCmdResponse getFailureResponse(SocksCmdRequest request) {
        return new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4);
    }
}
