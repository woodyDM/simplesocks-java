package org.simplesocks.netty.server.proxy.relay;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.Encrypter;
import org.simplesocks.netty.common.encrypt.OffsetEncrypter;
import org.simplesocks.netty.common.protocol.ProxyDataMessage;
import org.simplesocks.netty.common.util.ServerUtils;
import org.simplesocks.netty.server.auth.AuthProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * target server data to local server
 * @author
 *
 */
@Slf4j
public class TargetServerDataHandler extends ChannelInboundHandlerAdapter {

	private Channel toLocalServerChannel;
	private RelayProxyDataHandler handler;
	private AuthProvider authProvider;
	private Encrypter encrypter = OffsetEncrypter.getInstance();
	public static final int INTERVAL = 1000;
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(2);
	private static final GlobalTrafficShapingHandler TRAFFIC_SHAPING_HANDLER = new GlobalTrafficShapingHandler(EXECUTOR_SERVICE, INTERVAL);

	static {
        EXECUTOR_SERVICE.scheduleAtFixedRate(()->{
            TrafficCounter counter = TRAFFIC_SHAPING_HANDLER.trafficCounter();
            BigDecimal s = new BigDecimal(1024*INTERVAL/1000);
            BigDecimal read = BigDecimal.valueOf(counter.lastReadThroughput()).divide(s,2, RoundingMode.HALF_UP);
            BigDecimal write = BigDecimal.valueOf(counter.lastWriteThroughput()).divide(s,2, RoundingMode.HALF_UP);
            log.info("[Speed] Read:{}KB/s  Write:{}KB/s", read,write);
        }, 0,3, TimeUnit.SECONDS);
    }

	public TargetServerDataHandler(Channel toLocalServerChannel, RelayProxyDataHandler handler,AuthProvider authProvider) {
		this.toLocalServerChannel = toLocalServerChannel;
		this.handler = handler;
		this.authProvider = authProvider;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		handler.onTargetChannelActive();
		ctx.pipeline().addFirst(TRAFFIC_SHAPING_HANDLER);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf bytes = (ByteBuf) msg;
		log.debug("write target server data to local server. len {}",bytes.readableBytes());
		try{
			int len = bytes.readableBytes();
			byte[] bytes1 = new byte[len];
			bytes.readBytes(bytes1);
			byte[] bytes2 = encrypter.encrypt(bytes1);
			ProxyDataMessage request = new ProxyDataMessage(bytes2);
            toLocalServerChannel.writeAndFlush(request).addListener(future -> {
            	if(!future.isSuccess()){
            		log.warn("Failed to write to local server channel!");
				}
			});
		}finally {
			ReferenceCountUtil.release(bytes);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		close(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ServerUtils.logException(log, cause);
		close(ctx);
	}


	private void close(ChannelHandlerContext ctx){
		ctx.channel().close();
		authProvider.remove(toLocalServerChannel);
		if(toLocalServerChannel!=null)
			toLocalServerChannel.close();
	}

}
