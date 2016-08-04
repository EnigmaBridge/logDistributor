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
import com.enigmabridge.log.distributor.db.dao.UserObjectDao;
import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.LogstashConfig;
import com.enigmabridge.log.distributor.db.model.SplunkConfig;
import com.enigmabridge.log.distributor.db.model.UserObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
    private UserObjectDao userObjectDao;

    @Autowired
    private ClientBuilder clientBuilder;

    @Autowired
    private EntityManager em;

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

    /**
     * Sets the client record to the database.
     * If client with same client id exists, it is removed and replaced with this new record.
     * Dump can be imported back to the database in this way.
     *
     * @param addClientReq client records to add.
     * @return response
     */
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
                // TODO: enqueue reload

            } catch(Exception e){
                LOG.error("Exception in adding client", e);
                return new ErrorResponse("Exception");
            }
        }

        return new ResultResponse();
    }

    /**
     * Adds new UO to existing client record.
     *
     * @param clientId client id to add object to
     * @param object object to add
     * @return response
     */
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
            client.addObject(object);
            clientDao.save(client);
            // TODO: enqueue reload

        } catch(Exception e){
            LOG.error("Exception when adding object", e);
            return new ErrorResponse("Exception");
        }

        return new ResultResponse();
    }

    /**
     * Adds object to client that has same api key in records.
     * This method is used when new UO was created but caller has no record which client it belongs to.
     * Server goes through user object database and tries to find a client which has the same api key.
     * The object is added to the client if only one client is using the same api key.
     * TODO: If there are conflicts (more clients), object is added to that one which uses "catcher" UO =
     * TODO: user object record with API key, uotype=-1, uoid=-1.
     *
     * @param object object to add
     * @return response
     */
    @Transactional
    @RequestMapping(value = ApiConfig.API_PATH + "/client/addObject", method = RequestMethod.POST)
    public GeneralResponse addObjectGuess(@RequestBody UserObject object){
        try {
            // Fetch all clients which have this api key.
            final TypedQuery<Client> query = em.createQuery("SELECT uo.client" +
                    " FROM UserObject uo" +
                    " WHERE uo.apiKey = :apiKey GROUP BY uo.client", Client.class);
            query.setParameter("apiKey", object.getApiKey());
            final List<Client> matches = query.getResultList();

            if (matches.size() > 1){
                return new ErrorResponse("API key is used by more than 1 client, cannot add");
            }

            final Client client = matches.get(0);

            // Duplicate detection
            for (UserObject userObject : client.getObjects()) {
                if (userObject.equals(object)){
                    return new ErrorResponse("Object already added");
                }
            }

            client.addObject(object);
            clientDao.save(client);
            // TODO: enqueue reload

        } catch(Exception e){
            LOG.error("Exception when adding object", e);
            return new ErrorResponse("Exception");
        }

        return new ResultResponse();
    }

    /**
     * Removes object from existing client record.
     *
     * @param clientId client to remove object from
     * @param object object to remove
     * @return response
     */
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
                    userObjectDao.delete(cur);
                    modified = true;
                    break;
                }
            }

            if (modified){
                return new ResultResponse();
                // TODO: enqueue reload

            } else {
                return new ErrorResponse("Object not found");
            }

        } catch(Exception e){
            LOG.error("Exception when removing object", e);
            return new ErrorResponse("Exception");
        }
    }

    /**
     * Updates existing client statistics forwarding configuration, preserving object list intact.
     *
     * @param clientId client id to update config to
     * @param newClient new client configuration
     * @return response
     */
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
                final LogstashConfig oldConfig = client.getLogstashConfig();
                if (oldConfig != null){
                    client.setLogstashConfig(null);
                    em.remove(oldConfig);
                }
                client.setLogstashConfig(clientBuilder.build(newClient.getLogstashConfig()));
            }

            if (newClient.isSplunkConfigSet()){
                final SplunkConfig oldConfig = client.getSplunkConfig();
                if (oldConfig != null){
                    client.setSplunkConfig(null);
                    em.remove(oldConfig);
                }
                client.setSplunkConfig(clientBuilder.build(newClient.getSplunkConfig()));
            }

            clientDao.save(client);
            return new ResultResponse();
            // TODO: enqueue reload

        } catch(Exception e){
            LOG.error("Exception when configuring stats settings", e);
            return new ErrorResponse("Exception");
        }
    }


}
