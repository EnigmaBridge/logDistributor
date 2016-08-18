package com.enigmabridge.log.distributor.rest;

import com.enigmabridge.log.distributor.Stats;
import com.enigmabridge.log.distributor.utils.Utils;
import com.enigmabridge.log.distributor.api.ApiConfig;
import com.enigmabridge.log.distributor.api.response.ErrorResponse;
import com.enigmabridge.log.distributor.api.response.GeneralResponse;
import com.enigmabridge.log.distributor.api.response.LogResponse;
import com.enigmabridge.log.distributor.db.ClientBuilder;
import com.enigmabridge.log.distributor.db.DbHelper;
import com.enigmabridge.log.distributor.db.dao.ClientDao;
import com.enigmabridge.log.distributor.db.dao.UserObjectDao;
import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.UserObject;
import com.enigmabridge.log.distributor.forwarder.Router;
import com.enigmabridge.log.distributor.utils.DomainUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * Created by dusanklinec on 16.08.16.
 */
@Component
public class LogicManager {
    private final static Logger LOG = LoggerFactory.getLogger(LogicManager.class);

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private UserObjectDao userObjectDao;

    @Autowired
    private ClientBuilder clientBuilder;

    @Autowired
    private Router router;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private Stats stats;

    @Autowired
    private EntityManager em;

    /**
     * Accepts configuration from the site server.
     *
     * http://site2.enigmabridge.com:12000/1.0/testAPI/GetAllAPIKeys/sdfgsgf
     * {"function":"GetAllAPIKeys","result":{"API_TEST":{"use":[16,17,...,39030],"domain":"DEVELOPMENT","manage":[]}},"status":"9000","statusdetail":"success (ok)","version":"1.0"}
     *
     * @param jsonStr json string to process
     * @return response
     */
    @Transactional
    @RequestMapping(value = ApiConfig.API_PATH + "/client/site/configure", method = RequestMethod.POST)
    public GeneralResponse processSiteDump(@RequestBody String jsonStr){
        final String FIELD_RESULT = "result";
        final String FIELD_DOMAIN = "domain";
        final String FIELD_USE = "use";

        final LogResponse resp = new LogResponse();

        // Load mapping:
        // (domain, apiKey) -> client
        //
        // In current DB model we load all user object grouped by clients.
        // This minimizes data to be read.
        final TypedQuery<UserObject> query = em.createQuery("SELECT uo" +
                " FROM UserObject uo" +
                " GROUP BY uo.client", UserObject.class);

        // Domain -> ApiKey -> Client
        final Map<String, Map<String, Client>> domainApiClient = new HashMap<>();
        for (UserObject uo : query.getResultList()){
            final String domain = DomainUtils.sanitize(uo.getClient().getDomain().getDomain());
            final String apiKey = uo.getApiKey();

            domainApiClient.putIfAbsent(domain, new HashMap<>());
            domainApiClient.get(domain).put(apiKey, uo.getClient());
        }

        // If (domain,apikey) -> client exists, update UOs for client.
        try {
            final JSONObject json = new JSONObject(jsonStr);
            final JSONObject apis = json.getJSONObject(FIELD_RESULT);

            final Iterator<String> keyIt = apis.keys();
            for(;keyIt.hasNext();) {
                final String apiKey = keyIt.next();
                final JSONObject apiObj = apis.getJSONObject(apiKey);
                final String domain = DomainUtils.sanitize(apiObj.getString(FIELD_DOMAIN));

                // If domain,apiKey is not in mapping, cannot continue -> we dont know to which client we should map.
                final Client cl = Utils.getMap(domainApiClient, domain, apiKey);
                if (cl == null) {
                    final String logLine = String.format("Unrecognized domain:apiKey %s:%s", domain, apiKey);
                    resp.addLine(logLine);
                    LOG.warn(logLine);
                    continue;
                }

                // Build set of currently existing UOs
                final Set<UserObject> uoExisting = new HashSet<>(cl.getObjects());

                // UOlist to add & remove.
                final List<UserObject> uos2add = new LinkedList<>();
                final Set<UserObject> uos2del = new HashSet<>(uoExisting);

                // Add only user objects not yet created.
                final JSONArray useArr = apiObj.getJSONArray(FIELD_USE);
                for (int idx2 = 0, ln2 = useArr.length(); idx2 < ln2; idx2++) {
                    final UserObject uo = new UserObject();
                    uo.setApiKey(apiKey);
                    uo.setClient(cl);
                    uo.setUoId(useArr.getInt(idx2));
                    uos2del.remove(uo);

                    if (!uoExisting.contains(uo)){
                        uos2add.add(uo);
                    }
                }

                // Keep configuration of the existing client record. Delete all user objects - will be replaced by
                // new user object list. Only domain is updated.
                if (!uos2del.isEmpty()){
                    userObjectDao.delete(uos2del);
                    cl.getObjects().removeAll(uos2del);
                    stats.incUORemoved(uos2del.size());
                }

                if (!uos2add.isEmpty()){
                    cl.addObjects(uos2add);
                    stats.incUOAdded(uos2add.size());
                }

                // Bulk size large collection of UOs. Better for performance.
                //dbHelper.bulkSave(uos);

                // Then save root object.
                clientDao.save(cl);
                stats.incHostResync();
            }

            em.flush();
        } catch(Exception e){
            stats.incHostResyncFailed();
            LOG.error("Exception in parsing input data", e);
            return new ErrorResponse("Exception in parsing input data");
        }

        router.reload(clientDao.findAll());
        return resp;
    }
}
