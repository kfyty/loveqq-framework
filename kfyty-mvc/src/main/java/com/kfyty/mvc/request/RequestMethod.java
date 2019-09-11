package com.kfyty.mvc.request;

public enum RequestMethod {
    GET,
    PUT,
    POST,
    DELETE;

    public static RequestMethod matchRequestMethod(String requestMethod) {
        switch (requestMethod.toUpperCase()) {
            case "PUT":
                return RequestMethod.PUT;
            case "POST":
                return RequestMethod.POST;
            case "DELETE":
                return RequestMethod.DELETE;
            default:
                return RequestMethod.GET;
        }
    }
}
