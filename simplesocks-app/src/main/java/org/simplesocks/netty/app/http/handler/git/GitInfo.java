package org.simplesocks.netty.app.http.handler.git;

import lombok.Data;
import lombok.Getter;

@Data
public class GitInfo {
    private Boolean hasProxyData;
    private Boolean isGlobalMode;
    private String httpProxy;
    private String httpsProxy;
    private Integer localPort;
}
