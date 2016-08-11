package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;

/**
 * Record represents one client - domain mapping.
 * There are more records in this table having clientId the same, but having different domain.
 *
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

    @ManyToOne(optional = false)
    @JoinColumn(name="domain_id", nullable=false)
    private Domain domain;

    /**
     * API keys configured for this client.
     * Client can have some API keys configured from the management server
     * yet have 0 UOs for now. For the case of event saying new UO was created
     * on given domain with given API_KEY we can pair the UO to the client.
     * Consistency between list of apiKeys and all api keys in objects array is
     * not explicitly checked, but it should hold client.objects.apiKeys is subset of client.apiKeys.
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client", fetch = FetchType.EAGER)
    private List<ApiKey2Client> apiKeys;

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

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public List<ApiKey2Client> getApiKeys() {
        if (apiKeys == null){
            apiKeys = new LinkedList<>();
        }
        return apiKeys;
    }

    public void setApiKeys(List<ApiKey2Client> apiKeys) {
        this.apiKeys = apiKeys;
    }
}
