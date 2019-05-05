package org.simplesocks.netty.common.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;

import java.io.IOException;

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

	public static void handleException(Logger log, Throwable t){
		if(t instanceof IOException){
			log.warn("IOException : {}",t.getMessage());
		}else{
			log.error("Exception happended :",t);
		}
	}

	private ServerUtils() {
	}
}
