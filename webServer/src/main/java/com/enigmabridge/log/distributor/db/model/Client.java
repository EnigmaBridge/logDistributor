package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by dusanklinec on 03.08.16.
 */
@Entity
@Table(indexes = {
        @Index(columnList = "clientId")
})
public class Client {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    @Column
    private String clientId;

    @OneToOne(cascade = CascadeType.ALL)
    private LogstashConfig logstashConfig;

    @OneToOne(cascade = CascadeType.ALL)
    private SplunkConfig splunkConfig;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client")
    private List<UserObject> objects;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<UserObject> getObjects() {
        return objects;
    }

    public void setObjects(List<UserObject> objects) {
        this.objects = objects;
    }

    public LogstashConfig getLogstashConfig() {
        return logstashConfig;
    }

    public void setLogstashConfig(LogstashConfig logstashConfig) {
        this.logstashConfig = logstashConfig;
    }

    public SplunkConfig getSplunkConfig() {
        return splunkConfig;
    }

    public void setSplunkConfig(SplunkConfig splunkConfig) {
        this.splunkConfig = splunkConfig;
    }
}
