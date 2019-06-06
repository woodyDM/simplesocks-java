package org.simplesocks.netty.app.http;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Json tool just simple enough for this project
 *
 * json value should consider escape string.
 */
@Deprecated
public class JsonUtils {


    public static String toJson(Object target){
        if(target==null){
            throw new NullPointerException("target is null.");
        }
        Map<String, Object> values = getValues(target);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        values.forEach((key,value)->{
            append(sb,key,value);
        });
        sb.deleteCharAt(sb.length()-1);
        sb.append("}");
        return sb.toString();
    }

    private static void append(StringBuilder sb, String field, Object value){

        sb.append("\"").append(field).append("\":");
        appendValue(sb, value);
    }

    @SuppressWarnings("deprecated")
    private static void  appendValue(StringBuilder sb, Object value){
        if(value instanceof String){
            sb.append("\"").append(value).append("\",");
        }else if(value instanceof Number){
            sb.append(value).append(",");
        }else if(value instanceof Collection||(value!=null && value.getClass().isArray())){
            Object[] vs ;
            if(value instanceof Collection){
                vs = ((Collection) value).toArray();
            }else{
                vs = (Object[])value;
            }

            if(vs.length==0){
                sb.append("[],");
            }else{
                sb.append("[");
                for(Object v:vs){
                    appendValue(sb, v);
                }
                sb.deleteCharAt(sb.length()-1);
                sb.append("],");
            }
        }else if(value instanceof Boolean){
            Boolean b = (Boolean)value;
            if(b)
                sb.append("true,");
            else{
                sb.append("false,");
            }
        }else if(value!=null){
            String json = toJson(value);
            sb.append(json).append(",");
        }else if(value ==null){
            sb.append("null,");
        }
    }


    private static Map<String,Object> getValues(Object target){
        Map<String,Object> values = new HashMap<>();
        Class<?> clazz = target.getClass();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for(PropertyDescriptor propertyDescriptor:propertyDescriptors){
                String name = propertyDescriptor.getName();
                Method readMethod = propertyDescriptor.getReadMethod();
                if(!readMethod.isAccessible())
                    readMethod.setAccessible(true);
                Object v = readMethod.invoke(target);
                if(!name.equalsIgnoreCase("class"))
                    values.put(name, v);
            }
            return values;
        } catch (IntrospectionException |IllegalAccessException |InvocationTargetException e) {
            throw new IllegalArgumentException("unable to getValue of "+target.getClass().getName());
        }
    }
}
