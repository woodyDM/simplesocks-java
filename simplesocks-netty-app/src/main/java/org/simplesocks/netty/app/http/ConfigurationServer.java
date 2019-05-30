package org.simplesocks.netty.app.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;

import java.util.Optional;

@Slf4j
public class ConfigurationServer {

    private int port;
    private EventLoopGroup group;
    private AppConfiguration configuration;

    public ConfigurationServer(int port, EventLoopGroup group, AppConfiguration configuration) {
        this.port = port;
        this.group = group;
        this.configuration = configuration;
    }

    public Optional<ChannelFuture> start(){
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group,group)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new HttpConfigInitializer(configuration));
        try{
            ChannelFuture bind = bootstrap.bind(port).syncUninterruptibly();
            bind.addListener(f->{
                if(f.isSuccess())
                    log.info("Local config server started. Visit http://localhost:{} to customize.",port,port);
            });
            return Optional.of(bind);
        }catch (Exception e){
            log.error("Failed to start config server at port [{}]",port);
            return Optional.empty();
        }
    }



}
