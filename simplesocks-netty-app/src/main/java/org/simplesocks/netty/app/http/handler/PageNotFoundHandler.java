package org.simplesocks.netty.app.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.simplesocks.netty.app.config.AppConfiguration;
import org.simplesocks.netty.app.http.handler.base.ContentValueHandler;



public class PageNotFoundHandler extends ContentValueHandler {


    final String TEXT = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>404</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "<h2>\n" +
            "    404 Page Not Found<br>\n" +
            "    <a href=\"/index.html\">Return / 返回首页</a>\n" +
            "</h2>\n" +
            "</body>\n" +
            "</html>";


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
        returnContent(Constants.TEXT_HTML, TEXT, ctx, HttpResponseStatus.NOT_FOUND, msg);
    }
}
