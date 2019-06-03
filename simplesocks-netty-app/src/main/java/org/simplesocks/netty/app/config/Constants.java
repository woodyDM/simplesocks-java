package org.simplesocks.netty.app.config;

import java.io.File;

class Constants {


    public static final String PATH = "conf"+ File.separator+"config.xml";

    static final String XML_ROOT = "Configuration";
    static final String XML_LOCAL_PORT = "localPort";
    static final String XML_LOCAL_SERVER_PORT = "configServerPort";
    static final String XML_AUTH = "auth";
    static final String XML_REMOTE_HOST = "remoteHost";
    static final String XML_REMOTE_PORT = "remotePort";
    static final String XML_ENCRYPT_TYPE = "encryptType";
    static final String XML_GLOBAL_TYPE = "globalProxy";

}
