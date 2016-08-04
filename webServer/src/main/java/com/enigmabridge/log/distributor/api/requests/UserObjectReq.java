package com.enigmabridge.log.distributor.api.requests;

/**
 * Created by dusanklinec on 04.08.16.
 */
public class UserObjectReq {
    private String apiKey;

    private Integer uoType;

    private Integer uoId;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Integer getUoType() {
        return uoType;
    }

    public void setUoType(Integer uoType) {
        this.uoType = uoType;
    }

    public Integer getUoId() {
        return uoId;
    }

    public void setUoId(Integer uoId) {
        this.uoId = uoId;
    }
}
