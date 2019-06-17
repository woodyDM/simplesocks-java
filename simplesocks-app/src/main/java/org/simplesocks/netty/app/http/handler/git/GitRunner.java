package org.simplesocks.netty.app.http.handler.git;


import org.simplesocks.netty.app.utils.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 *
 */
public class GitRunner {

    public static final String ADD = "git config --global http.proxy socks5://127.0.0.1:{port}";
    public static final String ADDS = "git config --global https.proxy socks5://127.0.0.1:{port}";
    public static final String LS = "git config --global -l";
    public static final String DELETE = "git config --global --unset http.proxy";
    public static final String DELETES = "git config --global --unset https.proxy";
    public static final String KEY_HTTP = "http.proxy";
    public static final String KEY_HTTPS = "https.proxy";

    public static Tuple<String,String> getNowProxy(){
        Optional<List<String>> strings = runCmd(LS);
        Tuple<String,String> result = new Tuple<>();
        strings.ifPresent(list->list.forEach(s-> {
            String server = null;
            server = parse(KEY_HTTP,s);
            if(server!=null)
                result.setKey(server);
            server = parse(KEY_HTTPS,s);
            if(server!=null)
                result.setValue(server);
        }));
        return result;
    }

    private static String parse(String key,String line){
        if(line!=null){
            String[] words = line.split("=");
            if(words.length==2 && key.equalsIgnoreCase(words[0])){
                String server = words[1];

                return server;
            }
        }
        return null;
    }

    public static boolean addProxy(int port){
        String cmd1 = ADD.replace("{port}",port+"");
        String cmd2 = ADDS.replace("{port}",port+"");
        boolean b1 = runCmd(cmd1).isPresent();
        boolean b2 = runCmd(cmd2).isPresent();

        return b1 && b2;
    }

    public static boolean unsetProxy(){
        boolean b1 = runCmd(DELETE).isPresent();
        boolean b2 = runCmd(DELETES).isPresent();
        return b1 && b2;
    }

    private static Optional<List<String>> runCmd(String cmd){
        try{
            Process pro = Runtime.getRuntime().exec(cmd);
            int code = pro.waitFor();
            InputStream in = pro.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            String line = null;
            List<String> lines = new ArrayList<>();
            while((line = read.readLine())!=null){
                lines.add(line);
            }
            return Optional.of(lines);
        }catch (Exception e){
            return Optional.empty();
        }

    }
}
