package com.enigmabridge.log.distributor.api.response;

/**
 * Simple success response
 *
 * Created by dusanklinec on 01.08.16.
 */
public class ResultResponse extends GeneralResponse {
    protected boolean success=true;

    /**
     * Result of the operation.
     * Might be JSONObject or another serializable object
     */
    protected Object result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
