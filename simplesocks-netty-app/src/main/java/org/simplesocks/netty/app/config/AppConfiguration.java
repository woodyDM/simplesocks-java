package org.simplesocks.netty.app.config;

import lombok.Getter;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


/**
 *
 *
 *
 */
@Getter
@Slf4j
@ToString
public class AppConfiguration {

	private int localPort = 10800;
    private String encryptType="aes-cbc";

    private String auth;
    private String remoteHost;
	private int remotePort = 12000;
	private boolean globalProxy = false;


    public void configureLocalPort(int localPort) {
        checkPort("localPort", localPort);
        this.localPort = localPort;
    }

    public void configureEncryptType(String encryptType) {
        checkString("encryptType",encryptType);
        this.encryptType = encryptType;
    }

    public void configureAuth(String auth) {
        checkString("auth",auth);
        this.auth = auth;
    }

    public void configureRemoteHost(String remoteHost) {
        checkString("remoteHost",remoteHost);
        this.remoteHost = remoteHost;
    }

    public void configureRemotePort(int remotePort) {
        checkPort("remotePort", remotePort);
        this.remotePort = remotePort;
    }

    public void configureGlobalProxy(String globalProxy) {
        if(globalProxy==null||globalProxy.length()==0) return;
        if("true".equalsIgnoreCase(globalProxy)){
            this.globalProxy = true;
        }else if("false".equalsIgnoreCase(globalProxy)){
            this.globalProxy = false;
        }else{
            throw new IllegalArgumentException("globalProxy should be 'true' or 'false'.");
        }
    }



    private void checkPort(String field, int port){
        if(port<=0 || port>(1<<16)){
            throw new IllegalArgumentException("invalid port value : "+port+" for ["+field+"]");
        }
    }

    private void checkString(String field, String content){
        if(content==null||content.length()==0){
            throw new IllegalArgumentException(field+" should not be empty.");
        }
    }




}
