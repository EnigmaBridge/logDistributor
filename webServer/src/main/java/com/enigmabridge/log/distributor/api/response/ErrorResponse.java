package com.enigmabridge.log.distributor.api.response;

import org.json.JSONObject;

/**
 * Indicates error in the request processing.
 *
 * Created by dusanklinec on 01.08.16.
 */
public class ErrorResponse extends GeneralResponse {
    protected int errorCode = -1;
    protected String error;
    protected JSONObject cause;

    public ErrorResponse() {
    }

    public ErrorResponse(int errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorResponse(String error) {
        this.errorCode = -1;
        this.error = error;
    }

    public ErrorResponse(int errorCode, String error) {
        this.errorCode = errorCode;
        this.error = error;
    }

    public ErrorResponse(int errorCode, String error, JSONObject cause) {
        this.errorCode = errorCode;
        this.error = error;
        this.cause = cause;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getError() {
        return error;
    }

    public JSONObject getCause() {
        return cause;
    }

    public ErrorResponse setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ErrorResponse setError(String error) {
        this.error = error;
        return this;
    }

    public ErrorResponse setCause(JSONObject cause) {
        this.cause = cause;
        return this;
    }
}
