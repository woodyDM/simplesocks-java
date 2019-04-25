package org.shadowsocks.netty.client;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.shadowsocks.netty.client.config.Config;
import org.shadowsocks.netty.client.config.ConfigXmlLoader;
import org.shadowsocks.netty.client.config.PacLoader;
import org.shadowsocks.netty.client.manager.RemoteServerManager;
import org.shadowsocks.netty.client.mbean.IoAcceptorStat;
import org.shadowsocks.netty.client.proxy.SocksServerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

public class SocksServer {

	private static Logger logger = LoggerFactory.getLogger(SocksServer.class);

	private static final String CONFIG = "conf/config.xml";

	private static final String PAC = "conf/pac.xml";

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap bootstrap = null;
	private GlobalTrafficShapingHandler trafficHandler;

	private static SocksServer socksServer = new SocksServer();

	public static SocksServer getInstance() {
		return socksServer;
	}

	private SocksServer() {

	}

	public void start() {
		try {
			logger.info("user.dir {}",System.getProperty("user.dir"));
			Config config = ConfigXmlLoader.load(CONFIG);
			PacLoader.load(PAC);
			RemoteServerManager.init(config);

			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			bootstrap = new ServerBootstrap();
			trafficHandler = new GlobalTrafficShapingHandler(Executors.newScheduledThreadPool(1), 1000);

			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new SocksServerInitializer(trafficHandler));

			logger.info("Start At Port " + config.getLocalPort());
			startMBean();
			bootstrap.bind(config.getLocalPort()).sync().channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			stop();
		}
	}

	public void stop() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
		logger.info("Stop Server!");
	}

	/**
	 * java MBean 进行流量统计
	 */
	private void startMBean() {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		IoAcceptorStat mbean = new IoAcceptorStat();

		try {
			ObjectName acceptorName = new ObjectName(mbean.getClass().getPackage().getName() + ":type=IoAcceptorStat");
			mBeanServer.registerMBean(mbean, acceptorName);
		} catch (Exception e) {
			logger.error("java MBean error", e);
		}
	}

	public TrafficCounter getTrafficCounter() {
		return trafficHandler.trafficCounter();
	}

}
