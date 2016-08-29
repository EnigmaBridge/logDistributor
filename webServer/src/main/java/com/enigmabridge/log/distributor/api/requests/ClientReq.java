package com.enigmabridge.log.distributor.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by dusanklinec on 04.08.16.
 */
public class ClientReq {
    private String clientId;

    private String domain;

    private LogstashConfigReq logstashConfig;

    private SplunkConfigReq splunkConfig;

    private List<UserObjectReq> objects;

    private List<String> apiKeys;

    @JsonIgnore
    private boolean logstashConfigSet=false;

    @JsonIgnore
    private boolean splunkConfigSet=false;

    public ClientReq() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public LogstashConfigReq getLogstashConfig() {
        return logstashConfig;
    }

    public void setLogstashConfig(LogstashConfigReq logstashConfig) {
        this.logstashConfig = logstashConfig;
        this.logstashConfigSet = true;
    }

    public SplunkConfigReq getSplunkConfig() {
        return splunkConfig;
    }

    public void setSplunkConfig(SplunkConfigReq splunkConfig) {
        this.splunkConfig = splunkConfig;
        this.splunkConfigSet = true;
    }

    public List<UserObjectReq> getObjects() {
        return objects;
    }

    public void setObjects(List<UserObjectReq> objects) {
        this.objects = objects;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(List<String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    @JsonIgnore
    public boolean isLogstashConfigSet() {
        return logstashConfigSet;
    }

    @JsonIgnore
    public boolean isSplunkConfigSet() {
        return splunkConfigSet;
    }
}
