package com.enigmabridge.log.distributor.utils;

import java.io.Serializable;

/**
 * Raw request to EB.
 * Created by dusanklinec on 28.04.16.
 */
public class EBRawRequest implements Serializable {
    public static final long serialVersionUID = 1L;

    /**
     * HTTP method to use for the request (POST/GET)
     */
    protected String method = EBConnector.METHOD_DEFAULT;

    /**
     * URL path. If POST is used, this is still added as the path segment.
     */
    protected String url;

    /**
     * Request body
     */
    protected String body;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
