package org.shadowsocks.netty.server.auth;

public interface AuthProvider {

    boolean tryAuthenticate(String password, String identifier);


    boolean authenticated(String remoteIdentifier);


    void remove(String identifier);

}
