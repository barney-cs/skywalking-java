package org.apache.skywalking.apm.plugin.spring.mvc.yjj.request.constant;

import org.apache.skywalking.apm.agent.core.context.tag.StringTag;

public class TraceConstants {

    public static final String YJJ_REQUEST_SPAN = "yjj_request";

    public static final String YJJ_HEADER_CLIENT_TYPE_KEY = "token_platform_client_type";
    public static final String YJJ_HEADER_TOKEN_KEY = "zhcaitoken";


    public static final StringTag YJJ_HEADER_TAG = new StringTag("yjj.http.headers");
    public static final StringTag YJJ_BODY_TAG = new StringTag("yjj.http.body");

}
