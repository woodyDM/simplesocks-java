package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.handler.base.ContentValueHandler;

import java.nio.charset.StandardCharsets;


public class PageNotFoundHandler extends ContentValueHandler {


    final byte[] TEXT_BYTES = ("<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>404</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "<h2>\n" +
            "    404 Page Not Found<br>\n" +
            "    <a href=\"/\">Return / 返回首页</a>\n" +
            "</h2>\n" +
            "</body>\n" +
            "</html>").getBytes(StandardCharsets.UTF_8);


    public static final PageNotFoundHandler INSTANCE = new PageNotFoundHandler();

    @Override
    public String pathSupport() {
        return null;
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, AppConfiguration configuration) {
        returnContent(Constants.TEXT_HTML, TEXT_BYTES, ctx, HttpResponseStatus.NOT_FOUND, msg);
    }
}
