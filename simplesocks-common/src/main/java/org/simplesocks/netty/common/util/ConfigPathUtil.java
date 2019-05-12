package org.simplesocks.netty.common.util;

import java.io.File;
import java.util.Objects;

public class ConfigPathUtil {


    /**
     * @param filePath relative path to usr.dir
     * @return
     */
    public static String getUserDirFullName(String filePath){
        Objects.requireNonNull(filePath);
        filePath = filePath.replace("/", File.separator);
        filePath = filePath.replace("\\",File.separator);
        String base = System.getProperty("user.dir");
        return base+File.separator+filePath;
    }

}
