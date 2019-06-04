package org.simplesocks.netty.app.config;

import lombok.Getter;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Getter
@Slf4j
@ToString
public class AppConfiguration {
    /**
     * local proxy port
     */
	private int localPort = 10800;      //only localPortChange should recreate localServer
    /**
     * local configuration server port
     */
	private int configServerPort = 10589;

    /**
     * remote proxy enc type.
     */
    private String encryptType="aes-cbc";
    /**
     * remote proxy password
     */
    private String auth;
    /**
     * remote proxy host name , support IP or Domain;
     */
    private String remoteHost;
    /**
     * remote proxy server port
     */
	private int remotePort = 12000;
    /**
     * is proxy all request
     * if false
     */
	private boolean globalProxy = false;

    /**
     * The target host in this list will never be proxied even if it is in pac list or globalProxy is true.
     */
	private List<String> whiteList = new ArrayList<>();

    /**
     * the target host in this list will always be proxied , no matter what globalProxy is .
     */
    private List<String> proxyList = new ArrayList<>();





    public void configureLocalPort(int localPort) {
        checkPort("localPort", localPort);
        this.localPort = localPort;
    }

    public void configureLocalConfigServerPort(int localConfigServerPort) {
        checkPort("localConfigServerPort", localConfigServerPort);
        this.configServerPort = localConfigServerPort;
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

    /**
     * save to local PATH
     * @return
     */
    public boolean dump(){
        return false;
    }

    public static AppConfiguration load(){
        return ConfigXmlLoader.load(Constants.PATH);
    }


}
