package com.enigmabridge.log.distributor.api.requests;

import java.util.List;

/**
 * Created by dusanklinec on 04.08.16.
 */
public class AddClientsRequest {
    protected List<ClientReq> clients;

    public AddClientsRequest() {
    }

    public AddClientsRequest(List<ClientReq> clients) {
        this.clients = clients;
    }

    public List<ClientReq> getClients() {
        return clients;
    }

    public void setClients(List<ClientReq> clients) {
        this.clients = clients;
    }
}
