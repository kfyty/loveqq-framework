package com.kfyty.loveqq.framework.web.core.request;

public enum RequestMethod {
    GET,
    HEAD,
    PUT,
    POST,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE;

    public static RequestMethod matchRequestMethod(String requestMethod) {
        String method = requestMethod.toUpperCase();
        for (RequestMethod value : RequestMethod.values()) {
            if (value.name().equals(method)) {
                return value;
            }
        }
        return GET;
    }
}
