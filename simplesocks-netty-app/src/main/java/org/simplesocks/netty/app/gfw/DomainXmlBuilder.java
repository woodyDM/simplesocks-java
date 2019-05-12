package org.simplesocks.netty.app.gfw;


import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.util.ConfigPathUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DomainXmlBuilder {

    public static void main(String[] args) {
        Set<String> parse = parse();
        buildXml(parse);


    }

    static final byte[] TITLE="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n".getBytes(StandardCharsets.UTF_8) ;
    static final byte[] ROOT="<sites>\n".getBytes(StandardCharsets.UTF_8)  ;
    static final byte[] ROOT2="</sites>".getBytes(StandardCharsets.UTF_8)  ;


    public static void buildXml(Set<String> sites){
        String fullPath = ConfigPathUtil.getUserDirFullName(Constants.PATH);
        log.info("Start to build pac.xml...");
        File file = new File(fullPath);
        FileOutputStream outputStream = null;
        if(!file.exists()){
            String parent = file.getParent();
            File p = new File(parent);
            boolean mkdirs = p.mkdirs();
            log.info("Mkdir for {}:{}",parent, mkdirs);
        }
        try {
            if(file.exists()){
                log.info("The pac.xml exists, delete and create new one.");
                file.delete();
            }
            boolean ok = file.createNewFile();
            if(!ok){
                log.warn("Failed to create {}", fullPath);
                return;
            }
            outputStream = new FileOutputStream(file,false);

            outputStream.write(TITLE);
            outputStream.write(ROOT);
            for(String s : sites){
                String n = getNode("site", s);
                byte[] nb = n.getBytes(StandardCharsets.UTF_8);
                outputStream.write(nb);
            }
            outputStream.write(ROOT2);
            outputStream.flush();
        } catch (IOException e) {
            log.warn("IOException ,close building.");
        }finally {
            if(outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    //ignore.
                }
            }
        }
        log.info("Create pac.xml success.");

    }

    private static String getNode(String name,String value){
        return "\t<"+name+">"+value+"</"+name+">\n";
    }

    private static Set<String> parse(){
        BufferedReader reader = null;
        try{
            URL url = new URL(Constants.SOURCE);
            try(InputStream in=url.openStream()){
                reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line = null;
                log.info("Create buffer and connecting...");
                while((line=reader.readLine())!=null){
                    sb.append(line);
                }
                log.info("All data received, converting...");
                byte[] plain = Base64.getDecoder().decode(sb.toString());
                String txt = new String(plain, StandardCharsets.UTF_8);
                String[] lines = txt.split("\n");
                Set<String> result = Arrays.stream(lines)
                        .map(DomainXmlBuilder::parseOneLine)
                        .filter(it -> it != null && it.length() > 0)
                        .collect(Collectors.toSet());
                return result;
            }finally {
                if(reader!=null)
                    reader.close();
            }
        } catch (IOException e) {
            log.info("Failed to download list.. ");
            return new HashSet<>();
        }


    }

    /**
     *
     * @param line
     * @return
     */
    private static String parseOneLine(String line){
        if(line==null)
            return null;
        line = line.trim().replace("\n","").replace("\t","");
        if(line.length()==0)
            return null;
        if(line.contains("[")||line.contains("!")||line.contains("@@"))
            return null;
        if(line.contains("http")||line.contains("/"))
            return null;
        if(line.startsWith("||"))
            return line.substring(2);
        if(line.startsWith("."))
            return line.substring(1);

        return null;
    }
}
