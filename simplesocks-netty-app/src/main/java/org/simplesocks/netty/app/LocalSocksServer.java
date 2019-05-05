package org.simplesocks.netty.app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import org.simplesocks.netty.app.manager.CompositeRelayClientManager;
import org.simplesocks.netty.app.manager.SimpleSocksRelayClientManager;
import org.simplesocks.netty.app.mbean.IoAcceptorStat;
import org.simplesocks.netty.app.proxy.SocksServerInitializer;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.util.ServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;

/**
 * SOCKS5 本地代理
 */
public class LocalSocksServer implements Runnable{

	private static Logger logger = LoggerFactory.getLogger(LocalSocksServer.class);
	private static final String CONFIG = "conf/config.xml";
	private static final String PAC = "conf/pac.xml";

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap bootstrap = null;
	private GlobalTrafficShapingHandler trafficHandler;

	private static LocalSocksServer localSocksServer = new LocalSocksServer();

	public static LocalSocksServer getInstance() {
		return localSocksServer;
	}

	private LocalSocksServer() {}


	public static void main(String[] args) {
		getInstance().run();
	}

	/**
	 * 入口
	 */
	@Override
	public void run() {
		try {
			logger.info("'user.dir' is {}",System.getProperty("user.dir"));
			int port = 10800;
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
//			RelayClientManager manager = new CompositeRelayClientManager("localhost",10900,"123456笑脸☺", workerGroup);
//			RelayClientManager manager = new CompositeRelayClientManager("35.229.240.146",10900,"123456笑脸☺", workerGroup);
//			RelayClientManager manager = new DirectRelayClientManager(workerGroup);
			RelayClientManager manager = new SimpleSocksRelayClientManager("localhost",10900,"123456笑脸☺", workerGroup);

			bootstrap = new ServerBootstrap();
			trafficHandler = new GlobalTrafficShapingHandler(Executors.newScheduledThreadPool(1), 1000);

			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new SocksServerInitializer(trafficHandler,manager));

			logger.info("Start At Port {} "  ,port);
			//startMBean();
			bootstrap.bind(port).sync().
					channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("start error", e);
		} finally {
			stop();
		}
	}

	public void stop() {
		ServerUtils.closeEventLoopGroup(bossGroup);
		ServerUtils.closeEventLoopGroup(workerGroup);
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
