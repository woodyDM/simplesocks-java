package org.simplesocks.netty.app.config;

import org.simplesocks.netty.common.util.ConfigPathUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;


public class ConfigXmlWriter {

    public static boolean dump(AppConfiguration appConfiguration, String path){
        String targetFile = ConfigPathUtil.getUserDirFullName(path);
        File file = new File(targetFile);
        try(FileOutputStream out = new FileOutputStream(file)){
            StringBuilder content = getContent(appConfiguration);
            byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
            out.flush();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static StringBuilder getContent(AppConfiguration appConfiguration){
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<Configuration>\n");
        append(sb, 1, Constants.XML_LOCAL_PORT,  appConfiguration.getLocalPort()+"");
        append(sb, 1, Constants.XML_LOCAL_SERVER_PORT, appConfiguration.getConfigServerPort()+"");
        append(sb, 1, Constants.XML_AUTH, appConfiguration.getAuth());
        append(sb, 1, Constants.XML_REMOTE_HOST, appConfiguration.getRemoteHost());
        append(sb, 1, Constants.XML_REMOTE_PORT, appConfiguration.getRemotePort()+"");
        append(sb, 1, Constants.XML_ENCRYPT_TYPE, appConfiguration.getEncryptType());
        append(sb, 1, Constants.XML_GLOBAL_TYPE, appConfiguration.isGlobalProxy()+"");
        BiConsumer<String, List<String>> listWriter = (root,list)->{
            appendRoot(sb, 1,root, ()->{
                list.forEach(site->{
                    append(sb, 2,Constants.XML_SITE, site);
                });
            });
        };
        listWriter.accept(Constants.XML_WHITE_LIST, appConfiguration.getWhiteList());
        listWriter.accept(Constants.XML_PROXY_LIST, appConfiguration.getProxyList());
        sb.append("</Configuration>\n");
        return sb;
    }

    private static void appendRoot(StringBuilder sb, int tabSize, String f, Runnable childAppender){
        repeatTab(sb, tabSize);
        sb.append("<").append(f).append(">\n");
        childAppender.run();
        repeatTab(sb,tabSize);
        sb.append("</").append(f).append(">").append("\n");
    }

    private static void repeatTab(StringBuilder sb, int tabSize){
        for (int i = 0; i < tabSize; i++) {
            sb.append("\t");
        }
    }

    private static void append(StringBuilder sb,int tabSize, String f,Object value){
        repeatTab(sb, tabSize);
        sb.append("<").append(f).append(">").append(value).append("</").append(f).append(">").append("\n");
    }
}
