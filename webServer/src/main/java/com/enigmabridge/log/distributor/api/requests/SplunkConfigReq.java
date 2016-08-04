package com.enigmabridge.log.distributor.api.requests;

/**
 * Created by dusanklinec on 04.08.16.
 */
public class SplunkConfigReq {
    /**
     * Contains scheme + IP + port
     * e.g., tcp://192.168.0.1:7777
     */
    private String endpoint;

    /**
     * Auth token for the endpoint
     */
    private String token;

    /**
     * String of PEM CA roots. Required for TLS connection.
     */
    private String caRoots;

    /**
     * Whether to verify TLS or not.
     */
    private Boolean verifyTls = true;

    public SplunkConfigReq() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCaRoots() {
        return caRoots;
    }

    public void setCaRoots(String caRoots) {
        this.caRoots = caRoots;
    }

    public Boolean getVerifyTls() {
        return verifyTls;
    }

    public void setVerifyTls(Boolean verifyTls) {
        this.verifyTls = verifyTls;
    }
}
