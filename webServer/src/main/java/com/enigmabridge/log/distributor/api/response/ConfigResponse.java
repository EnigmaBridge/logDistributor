package com.enigmabridge.log.distributor.api.response;

import com.enigmabridge.log.distributor.db.model.Domain;
import com.enigmabridge.log.distributor.db.model.EBHost;

import java.util.List;

/**
 * Created by dusanklinec on 11.08.16.
 */
public class ConfigResponse extends ClientResponse {
    private List<EBHost> hosts;
    private List<Domain> domains;

    public List<EBHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<EBHost> hosts) {
        this.hosts = hosts;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }
}
