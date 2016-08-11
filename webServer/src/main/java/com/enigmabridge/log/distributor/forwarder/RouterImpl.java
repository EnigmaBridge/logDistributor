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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Routes messages to the destination described by current configuration.
 *
 * Created by dusanklinec on 08.08.16.
 */
@Component(value = ApiConfig.ROUTER)
@DependsOn(value = ApiConfig.YAML_CONFIG)
public class RouterImpl implements Router {
    private final static Logger LOG = LoggerFactory.getLogger(RouterImpl.class);

    /**
     * Domain -> domain routing configuration.
     */
    protected final Map<String, RoutingDomain> domains = new ConcurrentHashMap<>(8);

    @Autowired
    protected ClientDao clientDao;

    @PostConstruct
    @Async
    public void init(){
        // Will not be async - @Async required wrapping proxy to be used.
        reload(false);
    }

    @PreDestroy
    public void deinit(){
        shutdown();
    }

    @Async
    public Future<Integer> flush(){
        domains.entrySet().forEach(e -> e.getValue().flush());
        return new AsyncResult<>(1);
    }

    public Future<Integer> reload(){
        return reload(true);
    }

    public Future<Integer> reload(boolean lazy){
        final Iterable<Client> clients = clientDao.findAll();
        return reload(clients, lazy);
    }

    /**
     * Reloads complete routing with the client records.
     * The whole client database has to be provided.
     *
     * @param clients client database to use
     * @return future to track progress.
     */
    @Async
    public Future<Integer> reload(Iterable<Client> clients){
        return this.reload(clients, true);
    }

    /**
     * Reloads complete routing with the client records.
     * The whole client database has to be provided.
     *
     * @param clients client database to use
     * @param lazy if false, connectors are reinitialized even if configuration didnt change
     * @return future to track progress.
     */
    @Async
    public Future<Integer> reload(Iterable<Client> clients, boolean lazy){
        LOG.info("Reloading router[lazy={}]...", lazy);

        final LinkedList<Client> clientList = new LinkedList<>();
        clients.forEach(clientList::add);

        // Group by clients by domain
        final Map<String, List<Client>> clientsByDomain = clientList
                .stream()
                .collect(Collectors.groupingBy(RouterImpl::getDomain));

        // Compute domains to remove from the mapping - removed from configuration.
        final Set<String> removedDomains = new HashSet<>(domains.keySet());
        removedDomains.removeAll(clientsByDomain.keySet());

        // Process new client list, on the fly - with existing.
        for (Map.Entry<String, List<Client>> entry : clientsByDomain.entrySet()) {
            final String domain = entry.getKey();
            final List<Client> clientListForDomain = entry.getValue();

            RoutingDomain routingDomain = domains.get(domain);
            boolean alreadyAdded = routingDomain != null;
            removedDomains.remove(domain);

            if (!alreadyAdded){
                routingDomain = newDomain();
            }

            try {
                routingDomain.resync(clientListForDomain, true, lazy);
            } catch(Exception e){
                LOG.error("Exception in loading client", e);
            }

            if (!alreadyAdded){
                domains.put(domain, routingDomain);
            }
        }

        // Shutdown removed domains.
        for(String domain : removedDomains){
            final RoutingDomain routingDomain = domains.remove(domain);
            if (routingDomain != null) {
                routingDomain.shutdown();
            }
        }

        LOG.info("RouterImpl reloaded");
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
        LOG.info("Shutting down router");
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
        final String clDomain = client.getDomain().getDomain();
        return clDomain == null || clDomain.isEmpty() ? LogConstants.DEFAULT_DOMAIN : clDomain.toLowerCase();
    }

    public static String getDomain(JSONObject msg){
        // {"details":{"domain":"ddd"}}
        final Optional<String> detailsDomain = Utils.getAsJSON(msg, LogConstants.FIELD_DETAILS)
                .map(e -> Utils.getAsString(e, LogConstants.FIELD_DOMAIN))
                .orElse(Optional.empty());

        if (detailsDomain.isPresent()){
            return detailsDomain.get().toLowerCase();
        }

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
