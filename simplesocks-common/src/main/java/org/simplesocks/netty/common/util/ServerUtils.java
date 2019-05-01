package org.simplesocks.netty.common.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;

public final class ServerUtils {

	/**
	 * Closes the specified channel after all queued write requests are flushed.
	 */
	public static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
					ChannelFutureListener.CLOSE);
		}
	}

	public static void closeEventLoopGroup(EventLoopGroup group){
		if(group!=null){
			group.shutdownGracefully();
		}
	}

	private ServerUtils() {
	}
}
