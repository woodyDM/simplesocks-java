package org.shadowsocks.netty.server.auth;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryAuthProvider implements AuthProvider {

    private ConcurrentHashMap<String,Boolean> cache = new ConcurrentHashMap<>(256);

    @Override
    public boolean tryAuthenticate(String password, String identifier) {
        boolean ok = "123456笑脸☺".equals(password);
        Objects.requireNonNull(identifier);
        if(ok)
            cache.putIfAbsent(identifier,Boolean.TRUE);
        return ok;
    }

    @Override
    public boolean authenticated(String remoteIdentifier) {
        Objects.requireNonNull(remoteIdentifier);
        return cache.get(remoteIdentifier)!=null;
    }

    @Override
    public void remove(String identifier) {
        cache.remove(identifier);
    }
}
