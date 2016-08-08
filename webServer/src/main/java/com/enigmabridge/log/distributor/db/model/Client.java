package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
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

    @Column
    private String domain;

    @OneToOne(cascade = CascadeType.ALL)
    private LogstashConfig logstashConfig;

    @OneToOne(cascade = CascadeType.ALL)
    private SplunkConfig splunkConfig;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client", fetch = FetchType.EAGER)
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
        if (objects == null){
            objects = new LinkedList<>();
        }
        return objects;
    }

    public void addObject(UserObject uo){
        getObjects().add(uo);
        uo.setClient(this);
    }

    public void setObjects(List<UserObject> objects) {
        this.objects = objects;
        if (objects != null){
            for (UserObject object : objects) {
                object.setClient(this);
            }
        }
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
