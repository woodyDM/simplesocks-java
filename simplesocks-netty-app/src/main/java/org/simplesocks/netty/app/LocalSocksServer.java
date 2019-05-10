package org.simplesocks.netty.app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import org.simplesocks.netty.app.manager.CompositeRelayClientManager;
import org.simplesocks.netty.app.manager.SimpleSocksRelayClientManager;
import org.simplesocks.netty.app.mbean.IoAcceptorStat;
import org.simplesocks.netty.app.proxy.SocksServerInitializer;
import org.simplesocks.netty.common.encrypt.factory.CompositeEncrypterFactory;
import org.simplesocks.netty.common.netty.RelayClientManager;
import org.simplesocks.netty.common.util.ServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	private ScheduledExecutorService scheduledExecutorService = null;
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
			int port = 11800;
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();
			CompositeEncrypterFactory factory = new CompositeEncrypterFactory();
			String key = "123456笑脸☺";
			factory.registerKey(key.getBytes(StandardCharsets.UTF_8));
//			RelayClientManager manager = new CompositeRelayClientManager("localhost",10900,"123456笑脸☺", workerGroup);
//			RelayClientManager manager = new CompositeRelayClientManager("localhost",10900,"123456笑脸☺", workerGroup);
//			RelayClientManager manager = new CompositeRelayClientManager("35.229.240.146",11900,key, workerGroup,factory);
//			RelayClientManager manager = new DirectRelayClientManager(workerGroup);
			RelayClientManager manager = new SimpleSocksRelayClientManager("localhost",11900,key, workerGroup, factory);
			int interval = 1000;
			scheduledExecutorService = Executors.newScheduledThreadPool(2);
			bootstrap = new ServerBootstrap();
			trafficHandler = new GlobalTrafficShapingHandler(scheduledExecutorService, interval);
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new SocksServerInitializer(trafficHandler,manager));

			logger.info("Start At Port {} "  ,port);
			//startMBean();
			ChannelFuture channelFuture = bootstrap.bind(port);
			channelFuture.addListener(future -> {
				if(future.isSuccess()){
					scheduledExecutorService.scheduleAtFixedRate(()->{
						if(trafficHandler!=null){
							TrafficCounter counter = trafficHandler.trafficCounter();
							BigDecimal s = new BigDecimal(1024*interval/1000);
							BigDecimal read = BigDecimal.valueOf(counter.lastReadThroughput()).divide(s,2, RoundingMode.HALF_UP);
							BigDecimal write = BigDecimal.valueOf(counter.lastWriteThroughput()).divide(s,2, RoundingMode.HALF_UP);
							//logger.info("[Speed] Read:{}KB/s  Write:{}KB/s", read,write);
						}
					}, 0,3, TimeUnit.SECONDS);
				}
			});
			channelFuture.sync().channel().closeFuture().sync();
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
		if(scheduledExecutorService!=null){
			scheduledExecutorService.shutdownNow();
		}
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
