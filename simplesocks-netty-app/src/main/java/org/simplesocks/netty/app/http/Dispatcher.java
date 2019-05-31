package org.simplesocks.netty.app.http;



import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.http.handler.StaticResourceHandler;
import org.simplesocks.netty.common.exception.BaseSystemException;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {

    private Map<String, Map<String, HttpHandler>> data = new HashMap<>();
    private static final String index = "/";
    private static final String INDEX_PAGE = "/static/index.html";




    public void register(Class<? extends HttpHandler> clazz){
        try {
            HttpHandler httpHandler = clazz.newInstance();
            HttpMethod httpMethod = httpHandler.methodSupport();
            String path = httpHandler.pathSupport();
            register(path, httpMethod.name(), httpHandler);
        } catch (Exception e) {
            throw new BaseSystemException("Failed to register handler "+ clazz.getName());
        }
    }

    public void register(String path, String method, HttpHandler httpHandler){
        Map<String, HttpHandler> inMap = data.get(path);
        if(inMap==null){
            inMap = new HashMap<>();
            data.put(path, inMap);
        }
        inMap.put(method, httpHandler);
    }

    public HttpHandler get(String path,String method){
        if(path.equals(index)||path.startsWith(StaticResourceHandler.PATH)){
            return StaticResourceHandler.INSTANCE;
        }
        Map<String, HttpHandler> m = data.get(path);
        if(m==null){
            return StaticResourceHandler.INSTANCE;
        }else{
            return m.get(method);
        }
    }

}
