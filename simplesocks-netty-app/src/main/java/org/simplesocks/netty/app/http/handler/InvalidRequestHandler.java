package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.simplesocks.netty.app.http.handler.base.ContentValueHandler;

import java.nio.charset.StandardCharsets;


public class InvalidRequestHandler extends ContentValueHandler {


    final byte[] TEXT_BYTES = ("<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>400 invalid request</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "<h2>\n" +
            "    400 invalid request<br>\n" +
            "    <a href=\"/\">Return / 返回首页</a>\n" +
            "</h2>\n" +
            "</body>\n" +
            "</html>").getBytes(StandardCharsets.UTF_8);


    public static final InvalidRequestHandler INSTANCE = new InvalidRequestHandler();

    @Override
    public String pathSupport() {
        return null;
    }

    @Override
    public HttpMethod methodSupport() {
        return HttpMethod.GET;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        returnContent(Constants.TEXT_HTML, TEXT_BYTES, ctx, HttpResponseStatus.BAD_REQUEST, msg);
    }
}
