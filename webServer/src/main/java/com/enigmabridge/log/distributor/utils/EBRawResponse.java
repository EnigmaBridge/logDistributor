package com.enigmabridge.log.distributor.utils;

import java.io.Serializable;

/**
 * RAW response received from the EB.
 * Created by dusanklinec on 29.04.16.
 */
public class EBRawResponse implements Serializable {
    public static final long serialVersionUID = 1L;

    protected boolean successful = false;
    protected int httpCode;
    protected byte[] bodyBytes;
    protected String body;
    protected long responseTime;

    public EBRawResponse() {
    }

    public int getHttpCode() {
        return httpCode;
    }

    public EBRawResponse setHttpCode(int httpCode) {
        this.httpCode = httpCode;
        return this;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public EBRawResponse setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
        return this;
    }

    public String getBody() {
        return body;
    }

    public EBRawResponse setBody(String body) {
        this.body = body;
        return this;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public EBRawResponse setResponseTime(long responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public EBRawResponse setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }

    @Override
    public String toString() {
        return "EBRawResponse{" +
                "successful=" + successful +
                ", httpCode=" + httpCode +
                ", bodyBytes=" + Utils.byte2hexNullable(bodyBytes) +
                ", body='" + body + '\'' +
                ", responseTime=" + responseTime +
                '}';
    }
}
