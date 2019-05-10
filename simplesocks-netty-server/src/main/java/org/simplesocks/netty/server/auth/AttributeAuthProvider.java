package org.simplesocks.netty.server.auth;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * auth based on channel attributeKey
 */
@Slf4j
public class AttributeAuthProvider implements AuthProvider {

    private String auth;
    private static AttributeKey<Boolean> AUTH_KEY = AttributeKey.valueOf("auth");

    public AttributeAuthProvider(String auth) {
        this.auth = auth;
    }

    @Override
    public boolean tryAuthenticate(String auth, Channel channel) {
        boolean ok = this.auth.equals(auth);
        if(ok){
            Attribute<Boolean> attr = channel.attr(AUTH_KEY);
            attr.set(true);
        }
        return ok;
    }

    @Override
    public boolean authenticated(Channel channel) {
        boolean exist = channel.hasAttr(AUTH_KEY);
        if(!exist)
            return false;
        Attribute<Boolean> attr = channel.attr(AUTH_KEY);
        return attr.get();
    }

    @Override
    public void remove(Channel channel) {
        Attribute<Boolean> attr = channel.attr(AUTH_KEY);
        if(attr!=null)
            attr.set(false);
    }

}
