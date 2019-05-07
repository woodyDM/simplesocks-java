package org.simplesocks.netty.server.auth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface AuthProvider {


    boolean tryAuthenticate(String password, Channel channel);


    boolean authenticated(Channel channel);


    void remove(Channel channel);

}
