package org.simplesocks.netty.app.http.json;

import com.alibaba.fastjson.JSON;

import java.util.Objects;

public class FastJsonUtil implements JsonUtil {


    public static final FastJsonUtil INSTANCE = new FastJsonUtil();


    @Override
    public String toJson(Object obj) {
        Objects.requireNonNull(obj);
        return JSON.toJSONString(obj);
    }

    @Override
    public <T> T toJavaObject(String json, Class<T> clazz) {
        if(json==null||json.isEmpty())
            return null;
        T result = JSON.parseObject(json, clazz);
        return result;
    }
}
