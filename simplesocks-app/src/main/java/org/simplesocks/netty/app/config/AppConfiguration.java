package org.simplesocks.netty.app.config;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.utils.IOExecutor;
import org.simplesocks.netty.common.util.ConfigPathUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Getter
@Slf4j
@ToString
@Setter
public class AppConfiguration {
    /**
     * local proxy port
     */
	private int localPort = 10800;      //only localPortChange should recreate localServer
    /**
     * local configuration server port
     */
	private int configServerPort = 10590;

    /**
     * remote proxy enc type.
     */
    private String encryptType="aes-cbc";
    /**
     * remote proxy password
     */
    private String auth = "yourPassword";
    /**
     * remote proxy host name , support IP or Domain;
     */
    private String remoteHost = "localhost";
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



    public boolean isGeneralSame(AppConfiguration other){
        if(localPort!=other.localPort)
            return false;
        if(configServerPort!=other.configServerPort)
            return false;
        if(!encryptType.equals(other.encryptType))
            return false;
        if(!auth.equals(other.auth))
            return false;
        if(!remoteHost.equals(other.remoteHost))
            return false;
        if(remotePort!=other.remotePort)
            return false;
        if(globalProxy!=other.globalProxy)
            return false;
        return true;

    }

    private static boolean isSame(List<String> l1, List<String> l2){
        if(l1.size()!=l2.size())
            return false;
        boolean b1 = l1.stream().anyMatch(it -> !l2.contains(it));
        if(b1)
            return false;
        boolean b2 = l2.stream().anyMatch(it-> !l1.contains(it));
        return !b2;
    }


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

    public void addWhiteSite(String s){
        if(s!=null && s.length()>0)
            whiteList.add(s);
    }

    public void addProxySite(String s){
        if(s!=null && s.length()>0)
            proxyList.add(s);
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

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    public void setProxyList(List<String> proxyList) {
        this.proxyList = proxyList;
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

    public void dumpAsync(){
        IOExecutor.INSTANCE.submit(()->{
            dump();
        });
    }


    public boolean dump(){
        boolean ok = ConfigXmlWriter.dump(this, Constants.PATH_TEMP);
        if(ok){
            String realConfigFile = ConfigPathUtil.getUserDirFullName(Constants.PATH);
            String dumpConfigFile = ConfigPathUtil.getUserDirFullName(Constants.PATH_TEMP);
            File real = new File(realConfigFile);
            File dump = new File(dumpConfigFile);
            if(!real.exists() || real.delete())
                return dump.renameTo(real);
            else
                return false;
        }else{
            return false;
        }
    }



    public static AppConfiguration loadOrInit(){
        try {
            return ConfigXmlLoader.load(Constants.PATH);
        } catch (FileNotFoundException e) {
            AppConfiguration configuration = new AppConfiguration();
            configuration.dump();
            return configuration;
        }
    }

    public void mergeExceptDomainList(AppConfiguration other){
        AppConfiguration configuration = this;

        configuration.configureGlobalProxy(other.isGlobalProxy()+"");
        configuration.configureEncryptType(other.getEncryptType());
        configuration.configureRemoteHost(other.getRemoteHost());
        configuration.configureRemotePort(other.getRemotePort());
        configuration.configureLocalConfigServerPort(other.getConfigServerPort());
        configuration.configureAuth(other.getAuth());
        configuration.configureLocalPort(other.getLocalPort());
    }

}
