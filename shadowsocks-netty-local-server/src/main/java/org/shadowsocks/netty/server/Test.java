package org.shadowsocks.netty.server;

import lombok.extern.slf4j.Slf4j;
import org.shadowsocks.netty.common.protocol.ProxyDataRequest;
import org.shadowsocks.netty.common.protocol.ProxyRequest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class Test {

    public static void main(String[] args) {
        AtomicBoolean at = new AtomicBoolean(true);

        SimpleSocksProtocolClient client = new SimpleSocksProtocolClient("localhost",10900,"123456笑脸☺");

        client.setConnectionChannelListener(future -> {
            if(future.isSuccess()){
                log.info("CONNECT to remote ok!");
                client.setProxyDataRequestConsumer(request -> {
                    log.info(new String(request.getBytes(), StandardCharsets.UTF_8));
                    if(at.get()){
                        at.set(false);
                        client.endProxy().addListener(f2->{
                            client.sendProxyRequest("localhost",8085, ProxyRequest.Type.DOMAIN)
                                    .addListener(future1 -> {
                                        log.info("REQUEST OK2222!");
                                        byte[] b = "哈哈哈！烧掉图！".getBytes(StandardCharsets.UTF_8);
                                        client.sendProxyData(new ProxyDataRequest(b));
                                    });
                        });
                    }
                });
                client.sendProxyRequest("localhost",8087, ProxyRequest.Type.DOMAIN)
                        .addListener(future1 -> {
                            log.info("REQUEST OK!");
                            byte[] b = "哈哈哈！！".getBytes(StandardCharsets.UTF_8);
                            client.sendProxyData(new ProxyDataRequest(b));
                        });
            }
        });


        client.init();
    }
}
