package org.simplesocks.netty.app.http.handler.git;

import lombok.Data;

@Data
public class GitSettingRequest {


    private Type type;

    public enum Type{
       RESET,
       RESET_AND_NO_GLOBAL_PROXY,
       SET,
       SET_AND_GLOBAL_PROXY
    }

}
