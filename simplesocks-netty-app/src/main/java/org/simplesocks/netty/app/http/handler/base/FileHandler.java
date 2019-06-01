package org.simplesocks.netty.app.http.handler.base;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.http.HttpHandler;
import org.simplesocks.netty.app.utils.PathUtils;
import org.simplesocks.netty.common.util.ConfigPathUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * static resource handler
 * using eTag for simple cache control.
 */
@Slf4j
public abstract class FileHandler implements HttpHandler {

    private static final String ROOT;
    private static Map<String,String> eTags = new ConcurrentHashMap<>();


    static {
        if(PathUtils.getThisJarFilePath().isPresent()){
            ROOT = ConfigPathUtil.getRootFolder();
        }else{
            ROOT = ConfigPathUtil.getRootFolder() + "/target/classes";  //used when develop
        }
    }

    protected String parseContentType(String path){
        int i = path.lastIndexOf(".");
        if(i!=-1){
            String suffix = path.substring(i+1);
            if(suffix!=null) suffix = suffix.toLowerCase();
            switch (suffix){
                case "png":
                    return "image/png";
                case "html":
                    return "text/html;charset=utf-8";
                case "jpg":
                    return "image/jpeg";
                case "js":
                    return "application/x-javascript";
                case "css":
                    return "text/css";
            }
        }
        return "text/html;charset=utf-8";
    }

    protected void handle(ChannelHandlerContext ctx, FullHttpRequest msg, String filePath) throws FileNotFoundException{
        String requestTag = msg.headers().getAsString(HttpHeaderNames.IF_NONE_MATCH);

        if(requestTag==null||requestTag.isEmpty())
            handleNew(ctx, msg, filePath);
        else{
            String alreadyTag = eTags.get(filePath);
            if(!requestTag.equalsIgnoreCase(alreadyTag)){
                handleNew(ctx, msg, filePath);
            }else{
                handleCache(ctx, msg, filePath);
            }
        }
    }


    private void handleCache(ChannelHandlerContext ctx, FullHttpRequest msg, String filePath){
        HttpResponse response = new DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.NOT_MODIFIED);
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        if(keepAlive){
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        response.headers().set(HttpHeaderNames.IF_NONE_MATCH, false);
        ctx.write(response);
        endResponse(ctx, keepAlive);
    }

    private void handleNew(ChannelHandlerContext ctx, FullHttpRequest msg, String uriPath) throws FileNotFoundException {
        String fileFullPath = ROOT + uriPath;
        File file = new File(fileFullPath);

        RandomAccessFile accessFile = new RandomAccessFile(file, "r");
        HttpResponse response = new DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
        String contentType = parseContentType(uriPath);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        long length = file.length();
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        if(keepAlive){
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        String tag = getIfAbsent(uriPath);
        response.headers().set(HttpHeaderNames.IF_NONE_MATCH,true);
        response.headers().set(HttpHeaderNames.ETAG, tag);
        ctx.write(response);
        ctx.write(new DefaultFileRegion(accessFile.getChannel(), 0, file.length()));
        endResponse(ctx, keepAlive);
    }

    private void endResponse(ChannelHandlerContext ctx, boolean isKeepAlive){
        ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!isKeepAlive){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }



    private String getIfAbsent(String path){
        String s = eTags.get(path);
        if(s!=null)
            return s;
        String tag = UUID.randomUUID().toString();
        String old = eTags.putIfAbsent(path, tag);
        if(old==null){
            return tag;
        }else{
            return old;
        }
    }


}
