package com.enigmabridge.log.distributor.forwarder;

import com.enigmabridge.log.distributor.LogConstants;
import com.enigmabridge.log.distributor.Utils;
import com.enigmabridge.log.distributor.api.ApiConfig;
import com.enigmabridge.log.distributor.db.dao.ClientDao;
import com.enigmabridge.log.distributor.db.model.Client;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Created by dusanklinec on 08.08.16.
 */
@Service
@DependsOn(value = ApiConfig.YAML_CONFIG)
public class Router {
    private final static Logger LOG = LoggerFactory.getLogger(Router.class);

    /**
     * Domain -> domain routing configuration.
     */
    protected final Map<String, RoutingDomain> domains = new ConcurrentHashMap<>(8);

    @Autowired
    protected ClientDao clientDao;

    @PostConstruct
    public void init(){
        reload();
    }

    @PreDestroy
    public void deinit(){
        shutdown();
    }

    @Async
    @Transactional
    public Future<Integer> reload(){
        LOG.info("Reloading router...");

        final Iterable<Client> clients = clientDao.findAll();
        for(Client cl : clients){
            final String domain = getDomain(cl);

            RoutingDomain routingDomain = domains.get(domain);
            boolean alreadyAdded = routingDomain != null;

            if (!alreadyAdded){
                routingDomain = newDomain();
            }

            try {
                routingDomain.resync(cl);
            } catch(Exception e){
                LOG.error("Exception in loading client", e);
            }

            if (!alreadyAdded){
                domains.put(domain, routingDomain);
            }
        }

        LOG.info("Router reloaded");
        return new AsyncResult<Integer>(1);
    }

    /**
     * Takes input message from the processor and forwards it appropriatelly.
     *
     * @param jsonObject log message
     */
    public void processMessage(JSONObject jsonObject){
        try {
            final int uoid = getUserObject(jsonObject);
            final String domain = getDomain(jsonObject);

            LOG.info("domain: {}, uoid: {}, line: {}", domain, uoid, jsonObject);
            final RoutingDomain routingDomain = domains.get(domain);
            if (routingDomain == null){
                return;
            }

            routingDomain.forward(jsonObject);

        } catch(JSONException e){
            // Not a log message worth forwarding
        }
    }

    public void shutdown(){
        domains.entrySet().forEach(e -> e.getValue().shutdown());
    }

    @Lookup
    protected RoutingDomain newDomain(){
        return null;
    }

    public static int getUserObject(JSONObject msg){
        return Utils.getAsInteger(msg.getJSONObject(LogConstants.FIELD_DETAILS), LogConstants.FIELD_UO, 10);
    }

    public static String getDomain(Client client){
        final String clDomain = client.getDomain();
        return clDomain == null || clDomain.isEmpty() ? LogConstants.DEFAULT_DOMAIN : clDomain;
    }

    public static String getDomain(JSONObject msg){
        return getDomain(msg.getString(LogConstants.FIELD_SERVER));
    }

    public static String getDomain(String server){
        final int separatorIdx = server.indexOf("_");
        if (separatorIdx == -1){
            return LogConstants.DEFAULT_DOMAIN;
        }

        return server.substring(0, separatorIdx).toLowerCase();
    }
}
