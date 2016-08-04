package com.enigmabridge.log.distributor.api.requests;

import java.util.List;

/**
 * Created by dusanklinec on 04.08.16.
 */
public class ClientReq {
    private String clientId;

    private LogstashConfigReq logstashConfig;

    private SplunkConfigReq splunkConfig;

    private List<UserObjectReq> objects;

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
    }

    public SplunkConfigReq getSplunkConfig() {
        return splunkConfig;
    }

    public void setSplunkConfig(SplunkConfigReq splunkConfig) {
        this.splunkConfig = splunkConfig;
    }

    public List<UserObjectReq> getObjects() {
        return objects;
    }

    public void setObjects(List<UserObjectReq> objects) {
        this.objects = objects;
    }
}
