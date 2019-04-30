package org.simplesocks.netty.app.mbean;

import org.simplesocks.netty.app.LocalSocksServer;

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
