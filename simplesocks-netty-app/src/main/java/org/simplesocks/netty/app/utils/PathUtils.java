package org.simplesocks.netty.app.utils;



import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Optional;

public class PathUtils {

    /**
     * this file must be in netty-app project
     * @return
     */
    public static Optional<String> getThisJarFilePath(){
        URL location = PathUtils.class.getProtectionDomain().getCodeSource().getLocation();
        String fileName = location.getFile();
        boolean isRunningInJar = (fileName.endsWith(".jar"));
        if(isRunningInJar){
            if(fileName.startsWith("file:")){
                fileName = fileName.substring("file:".length());
            }
            try {
                fileName = URLDecoder.decode(fileName,"utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("failed to decode path to utf-8");
            }
            return Optional.of(fileName);
        }else{
            return Optional.empty();
        }
    }
}
