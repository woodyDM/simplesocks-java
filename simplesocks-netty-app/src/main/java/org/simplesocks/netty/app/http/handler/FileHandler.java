package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.HttpHandler;
import org.simplesocks.netty.app.utils.PathUtils;
import org.simplesocks.netty.common.util.ConfigPathUtil;

import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@Slf4j
public abstract class FileHandler implements HttpHandler {

    private static final String ROOT;

    static {
        if(PathUtils.getThisJarFilePath().isPresent()){
            ROOT = ConfigPathUtil.getRootFolder();
        }else{
            ROOT = ConfigPathUtil.getRootFolder() + "/target/classes";  //used when develop
        }
    }

    protected String parseContentType(String path){
        return "text/html;charset=utf-8";
    }


    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, String filePath) throws IOException, URISyntaxException {

        String path = ROOT + filePath;
        File file = new File(path);
        RandomAccessFile accessFile = new RandomAccessFile(file, "r");
        HttpResponse response = new DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
        String contentType = parseContentType(filePath);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        long length = file.length();
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        if(keepAlive){
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
        ctx.write(new DefaultFileRegion(accessFile.getChannel(), 0, file.length()));
        ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!keepAlive){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }



}
