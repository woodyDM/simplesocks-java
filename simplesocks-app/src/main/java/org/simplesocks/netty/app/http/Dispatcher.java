package org.simplesocks.netty.app.http;



import io.netty.handler.codec.http.HttpMethod;
import org.simplesocks.netty.app.http.handler.PageNotFoundHandler;
import org.simplesocks.netty.app.http.handler.StaticResourceHandler;
import org.simplesocks.netty.common.exception.BaseSystemException;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {

    private Map<String, Map<String, HttpHandler>> data = new HashMap<>();
    public static final String INDEX = "/";
    public static final String ICON = "/favicon.ico";



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
        method = method.toLowerCase();
        Map<String, HttpHandler> inMap = data.get(path);
        if(inMap==null){
            inMap = new HashMap<>();
            data.put(path, inMap);
        }
        inMap.put(method, httpHandler);
    }

    public HttpHandler get(String path,String method){

        method = method.toLowerCase();
        /**
         *  I know this looks bad ,same code can found in StaticResourceHandler.INSTANCE,
         *  Using handler.support() to decide handler is better.
         *  But it works for this <strong>simple</strong> server.
         */
        if(isSpecialStatic(path)){
            return StaticResourceHandler.INSTANCE;
        }
        Map<String, HttpHandler> m = data.get(path);
        if(m==null){
            return PageNotFoundHandler.INSTANCE;
        }else{
            HttpHandler httpHandler = m.get(method);
            if(httpHandler==null){
                return PageNotFoundHandler.INSTANCE;    //in fact should return 400
            }else{
                return httpHandler;
            }
        }
    }

    public static boolean isSpecialStatic(String path){
        return path.equals(INDEX)||path.equals(ICON)||path.startsWith(StaticResourceHandler.PATH)||path.startsWith("/umi");
    }

}
