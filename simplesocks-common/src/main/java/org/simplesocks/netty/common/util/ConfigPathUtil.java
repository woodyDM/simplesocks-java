package org.simplesocks.netty.common.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

public class ConfigPathUtil {


    /**
     * @param filePath relative path to usr.dir
     * @return
     */
    public static String getUserDirFullName(String filePath){
        Objects.requireNonNull(filePath);
        filePath = filePath.replace("/", File.separator);
        filePath = filePath.replace("\\",File.separator);
        String base = getRootFolder();
        return base+File.separator+filePath;
    }



    public static String getRootFolder(){
        File file = new File("");
        String path = file.getAbsolutePath();
        try {
            return URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("unable to get RootFolder.");
        }
    }



}
