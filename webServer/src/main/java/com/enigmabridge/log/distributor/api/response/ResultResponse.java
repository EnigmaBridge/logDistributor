package com.enigmabridge.log.distributor.api.response;

/**
 * Simple success response
 *
 * Created by dusanklinec on 01.08.16.
 */
public class ResultResponse extends GeneralResponse {
    protected boolean success=true;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
