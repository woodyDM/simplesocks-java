package org.shadowsocks.netty.client.mbean;

import org.shadowsocks.netty.client.LocalSocksServer;

public class IoAcceptorStat implements IoAcceptorStatMBean {

	@Override
	public long getWrittenBytesThroughput() {
		return LocalSocksServer.getInstance().getTrafficCounter()
				.lastWriteThroughput();
	}

	@Override
	public long getReadBytesThroughput() {
		return LocalSocksServer.getInstance().getTrafficCounter()
				.lastReadThroughput();
	}

}
