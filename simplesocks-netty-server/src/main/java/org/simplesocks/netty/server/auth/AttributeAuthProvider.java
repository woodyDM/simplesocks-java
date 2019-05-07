package org.simplesocks.netty.server.auth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AttributeAuthProvider implements AuthProvider {

    private String password;
    private AttributeKey<Boolean> token = AttributeKey.valueOf("auth");

    public AttributeAuthProvider(String password) {
        this.password = password;
    }

    @Override
    public boolean tryAuthenticate(String password, Channel channel) {
        boolean ok = this.password.equals(password);
        if(ok){
            Attribute<Boolean> attr = channel.attr(token);
            attr.set(true);
        }
        return ok;
    }

    @Override
    public boolean authenticated(Channel channel) {
        boolean exist = channel.hasAttr(token);
        if(!exist)
            return false;
        Attribute<Boolean> attr = channel.attr(token);
        return attr.get();
    }

    @Override
    public void remove(Channel channel) {
        Attribute<Boolean> attr = channel.attr(token);
        if(attr!=null)
            attr.set(false);
    }

}
