package org.simplesocks.netty.common.util;

public class StringUtils {

    public static String trim(String s){
        if(s==null||s.isEmpty())
            return null;
        s = s.trim();
        if(s.isEmpty())
            return null;
        return s;
    }
}
