package org.simplesocks.netty.app.utils;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;

public class IOExecutor {

    public static final EventLoopGroup INSTANCE = new DefaultEventLoopGroup(2);

}
