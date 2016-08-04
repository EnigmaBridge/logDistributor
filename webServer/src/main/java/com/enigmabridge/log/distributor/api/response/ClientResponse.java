package com.enigmabridge.log.distributor.api.response;

import com.enigmabridge.log.distributor.db.model.Client;

import java.util.List;

/**
 * Created by dusanklinec on 04.08.16.
 */
public class ClientResponse extends ResultResponse {
    List<Client> clients;

    public ClientResponse() {
    }

    public ClientResponse(List<Client> clients) {
        this.clients = clients;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }
}
