package org.simplesocks.netty.app.http.handler.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.simplesocks.netty.app.http.handler.Constants;
import org.simplesocks.netty.app.http.json.FastJsonUtil;
import org.simplesocks.netty.app.http.json.JsonUtil;


public abstract class JsonValueHandler extends ContentValueHandler{

    protected JsonUtil jsonUtil = FastJsonUtil.INSTANCE;



    protected void returnOkJsonContent(Object body, ChannelHandlerContext ctx, FullHttpRequest msg){
        returnJsonContent(body, ctx, HttpResponseStatus.OK, msg);
    }

    protected void returnJsonContent(Object body, ChannelHandlerContext ctx, HttpResponseStatus status, FullHttpRequest msg){
        String json = jsonUtil.toJson(body);
        returnContent(Constants.APPLICATION_JSON, json, ctx, status, msg);
    }
}
