package org.simplesocks.netty.app.http.json;

public interface JsonUtil {


    String toJson(Object obj);



    <T> T toJavaObject(String json, Class<T> clazz);

}
