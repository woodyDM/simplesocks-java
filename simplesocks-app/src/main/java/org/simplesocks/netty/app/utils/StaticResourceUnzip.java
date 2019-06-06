package org.simplesocks.netty.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.util.ConfigPathUtil;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * in order to use Zero-copy and non-blocking file transport, write static file to working root.
 * zip resource for config server
 * classpath:static/*
 */
@Slf4j
public class StaticResourceUnzip {


    static final String NAME = "static";

    public static boolean unzipStaticResource(){

        Optional<String> jarFilePath = PathUtils.getThisJarFilePath();
        if(jarFilePath.isPresent()){
            log.info("Try unzip resource from {}",jarFilePath.get());
            return unzipStaticResource0(jarFilePath.get());
        }else{
            return true;
        }
    }

    private static boolean unzipStaticResource0(String jarFile){
        String root = ConfigPathUtil.getRootFolder();
        createStaticFolder(root);
        try{
            unzipStaticResource(root, jarFile);
            return true;
        } catch (IOException e) {
            log.error("Failed to unzip "+ jarFile,e);
            return false;
        }
    }


    private static void createStaticFolder(String root){
        String f = root + File.separator + NAME;
        File file = new File(f);
        if(!file.exists()){
            file.mkdirs();
        }
    }


    private static void unzipStaticResource(String rootPath, String thisJarFile) throws IOException {
        final String folder = "static/";
        List<CompletableFuture> futures = new ArrayList<>();
        try (JarFile file = new JarFile(thisJarFile)) {
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()){
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if(name.startsWith(folder)&&!name.equalsIgnoreCase(folder)){
                    futures.add(CompletableFuture.runAsync(()->{
                        try {
                            unzipOneStaticResource(rootPath, name, jarEntry, file);
                        } catch (IOException e) {
                            throw new RuntimeException("exception unzip "+name, e);
                        }
                    }));
                }
            }
            if(futures.size()>0){
                CompletableFuture[] allTask = futures.toArray(new CompletableFuture[0]);
                CompletableFuture<Void> union = CompletableFuture.allOf(allTask);
                try {
                    Void aVoid = union.get();
                } catch (InterruptedException | ExecutionException e) {
                    //
                }
            }

        }

    }

    private static void unzipOneStaticResource(String root, String name, JarEntry entry, JarFile jarFile) throws IOException {
        String fullPath = root + File.separator + name;
        File file = new File(fullPath);
        if(!file.exists())
            file.createNewFile();
        else
            file.delete();
        log.info("Unzip: {}", fullPath);
        FileOutputStream fileOutputStream =null;
        InputStream inputStream = null;
        try{
            fileOutputStream = new FileOutputStream(file);
            inputStream = jarFile.getInputStream(entry);
            byte[] buff = new byte[4096];
            int len = -1;
            while ((len=inputStream.read(buff,0,4096))!= -1 ){
                fileOutputStream.write(buff,0,len);
            }
        }finally {
            close(fileOutputStream);
            close(inputStream);
        }
    }

    private static void close(Closeable closeable){
        if(closeable!=null){
            try {
                closeable.close();
            } catch (IOException e) {
                //
            }
        }
    }
}
