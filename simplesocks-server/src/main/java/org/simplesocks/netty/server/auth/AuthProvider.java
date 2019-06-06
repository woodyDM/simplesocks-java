package org.simplesocks.netty.server.auth;

import io.netty.channel.Channel;


public interface AuthProvider {


    boolean tryAuthenticate(String auth, Channel channel);


    boolean authenticated(Channel channel);


    void remove(Channel channel);

}
