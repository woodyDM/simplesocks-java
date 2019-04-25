package org.shadowsocks.netty.client.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.Closeable;
import java.io.IOException;


public class ConnectionPool implements Closeable {

    private GenericObjectPool<Connection> pool;
    private int maxSize;
    private ConnectionFactory factory;

    public ConnectionPool(int maxSize, ConnectionFactory factory) {
        GenericObjectPoolConfig<Connection> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(maxSize);
        this.maxSize = maxSize;
        this.factory = factory;
        this.pool = new GenericObjectPool<>(factory, config);
    }

    @Override
    public void close() throws IOException {
        if(pool!=null&&!pool.isClosed()){
            pool.close();
        }
    }
}
