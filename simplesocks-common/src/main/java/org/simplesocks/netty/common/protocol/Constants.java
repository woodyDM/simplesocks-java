package org.simplesocks.netty.common.protocol;

public class Constants {

    public static final byte VERSION1 = (byte)0x01;
    public static final int LEN_VERSION = 1;
    public static final int LEN_CONTENT_LENGTH = 4;
    public static final int LEN_CMD = 1;
    public static final int LEN_PROXY_TYPE = 1;
    public static final int LEN_PROXY_PORT = 2;
    public static final int LEN_PROXY_OFFSET = 1;
    public static final int LEN_HEAD = LEN_VERSION + LEN_CONTENT_LENGTH+LEN_CMD;
    public static final int LEN_PROXY =   LEN_PROXY_TYPE+LEN_PROXY_PORT+LEN_PROXY_OFFSET;


    public static final byte NO_AUTH = (byte)0x01;
    public static final byte AUTH = (byte)0x02;

    public static final int RESPONSE_SUCCESS = 0x01;
    public static final int RESPONSE_FAIL = 0x02;

    public static final int PROXY_TYPE_IPV4 = 0x01;
    public static final int PROXY_TYPE_DOMAIN = 0x03;
    public static final int PROXY_TYPE_IPV6 = 0x04;



}
