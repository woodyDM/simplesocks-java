package org.simplesocks.netty.app.utils;


import lombok.Getter;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class ProxyCounter {


    private LocalDateTime startTime = LocalDateTime.now();
    private AtomicLong directCounter = new AtomicLong(0);
    private AtomicLong proxyCounter = new AtomicLong(0);
    private AtomicLong failedCounter = new AtomicLong(0);

}
