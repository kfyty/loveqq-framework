package com.kfyty.mvc.request;

public enum RequestMethod {
    GET,
    PUT,
    POST,
    PATCH,
    DELETE;

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
