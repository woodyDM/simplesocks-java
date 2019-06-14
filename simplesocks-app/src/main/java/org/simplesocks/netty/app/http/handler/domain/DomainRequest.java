package org.simplesocks.netty.app.http.handler.domain;

import lombok.Data;

@Data
public class DomainRequest {
    private String type;
    private String domain;

    public boolean isValid(){
        if(type==null)
            return false;
        if(type.equals(Constants.TYPE_PROXY)||type.equals(Constants.TYPE_WHITE)){
            if(domain!=null&&!domain.isEmpty()){
                return true;
            }
        }
        return false;
    }
}
