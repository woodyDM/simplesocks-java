package org.shadowsocks.netty.client.mbean;

import org.shadowsocks.netty.client.SocksServer;

public class IoAcceptorStat implements IoAcceptorStatMBean {

	@Override
	public long getWrittenBytesThroughput() {
		return SocksServer.getInstance().getTrafficCounter()
				.lastWriteThroughput();
	}

	@Override
	public long getReadBytesThroughput() {
		return SocksServer.getInstance().getTrafficCounter()
				.lastReadThroughput();
	}

}
