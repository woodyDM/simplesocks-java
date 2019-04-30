package org.shadowsocks.netty.client.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.shadowsocks.netty.client.manager.RelayClientManager;
import org.shadowsocks.netty.client.proxy.AcceptClientConnectionHandler;


/**
 * 主程序初始化
 */
public final class SocksServerInitializer extends ChannelInitializer<SocketChannel> {



	private GlobalTrafficShapingHandler trafficHandler;
	private RelayClientManager relayClientManager;

	public SocksServerInitializer(GlobalTrafficShapingHandler trafficHandler, RelayClientManager manager) {
		this.trafficHandler = trafficHandler;
		this.relayClientManager = manager;
	}

	@Override
	public void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline p = socketChannel.pipeline();
		p.addLast(new SocksInitRequestDecoder());
		p.addLast(new SocksMessageEncoder());
		p.addLast(new AcceptClientConnectionHandler(relayClientManager));
		p.addLast(trafficHandler);
	}
}
