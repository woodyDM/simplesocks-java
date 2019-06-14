package org.simplesocks.netty.app.http.handler.base;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.simplesocks.netty.app.http.handler.InvalidRequestHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public abstract class RequestBodyHandler<T> extends JsonValueHandler {

    protected abstract void handle0(T body, ChannelHandlerContext ctx, FullHttpRequest msg);

    @Override
    public final void handle(ChannelHandlerContext ctx, FullHttpRequest msg) {
        ByteBuf content = msg.content();
        String body = content.toString(StandardCharsets.UTF_8);
        if(body==null||body.isEmpty()){
            InvalidRequestHandler.INSTANCE.handle(ctx, msg);
        }else{
            Class<T> targetType = parseClazz();
            try{
                T obj = JSON.parseObject(body, targetType);
                handle0(obj, ctx, msg);
            }catch (Exception e){
                InvalidRequestHandler.INSTANCE.handle(ctx, msg);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<T> parseClazz(){
        Class<?> clazz = this.getClass();
        do{
            Type sup = clazz.getGenericSuperclass();
            if(sup instanceof ParameterizedType){
                ParameterizedType t = (ParameterizedType) sup;
                Type actualTypeArgument = t.getActualTypeArguments()[0];
                return (Class<T>)actualTypeArgument;
            }else{
                clazz = clazz.getSuperclass();
            }
        }while (clazz!=null);
        throw new IllegalStateException("class "+this.getClass()+" is not generic.");

    }



}
