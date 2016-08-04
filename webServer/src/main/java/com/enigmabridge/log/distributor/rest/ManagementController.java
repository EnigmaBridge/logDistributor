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
import com.enigmabridge.log.distributor.db.dao.LogstashConfigDao;
import com.enigmabridge.log.distributor.db.dao.SplunkConfigDao;
import com.enigmabridge.log.distributor.db.dao.UserObjectDao;
import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.SplunkConfig;
import com.enigmabridge.log.distributor.db.model.UserObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
     * Business controller calls this on new UO was created successfully
     * @param uoHandle UO that was created
     * @return response
     */
    @RequestMapping("/")
    public GeneralResponse uoCreated(
            @RequestParam() String uoHandle
    ) {
        return new ErrorResponse("Not implemented yet");
    }

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
        }

        return new ResultResponse();
    }

}
