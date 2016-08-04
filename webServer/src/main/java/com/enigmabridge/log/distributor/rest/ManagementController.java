package com.enigmabridge.log.distributor.rest;

import com.enigmabridge.log.distributor.api.ApiConfig;
import com.enigmabridge.log.distributor.api.requests.AddClientsRequest;
import com.enigmabridge.log.distributor.api.requests.ClientReq;
import com.enigmabridge.log.distributor.api.response.ClientResponse;
import com.enigmabridge.log.distributor.api.response.ErrorResponse;
import com.enigmabridge.log.distributor.api.response.GeneralResponse;
import com.enigmabridge.log.distributor.api.response.ResultResponse;
import com.enigmabridge.log.distributor.db.ClientBuilder;
import com.enigmabridge.log.distributor.db.dao.ClientDao;
import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.UserObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Create new API Key calls, common administration stuff.
 *
 * Created by dusanklinec on 01.08.16.
 */
@RestController
@PreAuthorize("hasAuthority('"+ ApiConfig.MANAGEMENT_ROLE+"')")
public class ManagementController {
    private final static Logger LOG = LoggerFactory.getLogger(ManagementController.class);

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private ClientBuilder clientBuilder;

    /**
     * Dumps the whole client configuration.
     * @return client response
     */
    @RequestMapping(value = ApiConfig.API_PATH + "/client/list", method = RequestMethod.GET)
    public GeneralResponse dumpConfiguration(){
        final Iterable<Client> clients = clientDao.findAll();
        final ClientResponse resp = new ClientResponse();
        resp.setClients(new LinkedList<>());

        for (Client client : clients) {
            resp.getClients().add(client);
        }

        return resp;
    }

    @Transactional
    @RequestMapping(value = ApiConfig.API_PATH + "/client/add", method = RequestMethod.POST)
    public GeneralResponse addClient(@RequestBody AddClientsRequest addClientReq){
        final List<ClientReq> clientReqs = addClientReq.getClients();
        for (ClientReq clientReq : clientReqs) {
            final String clientId = clientReq.getClientId();

            try {
                final Client dbClient = clientBuilder.build(clientReq);
                final List<Client> deletedClients = clientDao.deleteByClientId(clientId);
                clientDao.save(dbClient);

            } catch(Exception e){
                LOG.error("Exception in adding client", e);
                return new ErrorResponse("Exception");
            }
        }

        return new ResultResponse();
    }

    @Transactional
    @RequestMapping(value = ApiConfig.API_PATH + "/client/addObject/{clientId}", method = RequestMethod.POST)
    public GeneralResponse addObject(@PathVariable(value = "clientId") String clientId,
                                     @RequestBody UserObject object
    ){
        try {
            final Client client = clientDao.findByClientId(clientId);
            if (client == null){
                return new ErrorResponse("Client not found");
            }

            // Already added?
            final List<UserObject> objects = client.getObjects();
            for (UserObject userObject : objects) {
                if (userObject.equals(object)){
                    return new ErrorResponse("Object already added");
                }
            }

            // Add
            client.getObjects().add(object);
            clientDao.save(client);

        } catch(Exception e){
            LOG.error("Exception when adding object", e);
            return new ErrorResponse("Exception");
        }

        return new ResultResponse();
    }

    @Transactional
    @RequestMapping(value = ApiConfig.API_PATH + "/client/removeObject/{clientId}", method = RequestMethod.POST)
    public GeneralResponse removeObject(@PathVariable(value = "clientId") String clientId,
                                        @RequestBody UserObject object
    ){
        try {
            final Client client = clientDao.findByClientId(clientId);
            if (client == null){
                return new ErrorResponse("Client not found");
            }

            // Already added?
            final List<UserObject> objects = client.getObjects();
            final Iterator<UserObject> iterator = objects.iterator();
            boolean modified = false;

            while(iterator.hasNext()){
                final UserObject cur = iterator.next();
                if (cur.equals(object)){
                    iterator.remove();
                    modified = true;
                    break;
                }
            }

            if (modified){
                clientDao.save(client);
                return new ResultResponse();

            } else {
                return new ErrorResponse("Object not found");
            }

        } catch(Exception e){
            LOG.error("Exception when removing object", e);
            return new ErrorResponse("Exception");
        }
    }

    @Transactional
    @RequestMapping(value = ApiConfig.API_PATH + "/client/config/{clientId}", method = RequestMethod.POST)
    public GeneralResponse updateStatsConfig(@PathVariable(value = "clientId") String clientId,
                                             @RequestBody ClientReq newClient
    ){
        try {
            final Client client = clientDao.findByClientId(clientId);
            if (client == null){
                return new ErrorResponse("Client not found");
            }

            if (newClient.isLogstashConfigSet()){
                client.setLogstashConfig(clientBuilder.build(newClient.getLogstashConfig()));
            }

            if (newClient.isSplunkConfigSet()){
                client.setSplunkConfig(clientBuilder.build(newClient.getSplunkConfig()));
            }

            clientDao.save(client);
            return new ResultResponse();

        } catch(Exception e){
            LOG.error("Exception when configuring stats settings", e);
            return new ErrorResponse("Exception");
        }
    }



}
