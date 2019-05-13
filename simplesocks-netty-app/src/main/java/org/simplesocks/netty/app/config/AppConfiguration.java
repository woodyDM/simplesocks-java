package org.simplesocks.netty.app.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 */
@Getter
@Slf4j
@Setter
@ToString
public class AppConfiguration {

	private int localPort = 10800;
    private String encryptType;

    private String auth;
    private String remoteHost;
	private int remotePort = 12000;
	private boolean forceProxy = false;





}
