package com.enigmabridge.log.distributor.forwarder;

import com.enigmabridge.log.distributor.db.model.Client;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Encapsulates routing for one routing domain.
 * In the routing domain UOIDs are unique and routing path based on the OUID can be found easily.
 * Manages all clients in the same routing domain.
 *
 * Created by dusanklinec on 08.08.16.
 */
@Component
@Scope("prototype")
public class RoutingDomain {
    private final static Logger LOG = LoggerFactory.getLogger(RoutingDomain.class);

    /**
     * All client forwarders.
     * client ID -> forwarder mapping
     */
    protected final Map<String, Forwarder> forwarders = new ConcurrentHashMap<>();

    /**
     * UOID -> forwarder mapping
     */
    protected final Map<Integer, Forwarder> uoidMap = new ConcurrentHashMap<>();

    /**
     * Synchronizes all clients in this domain.
     * Can remove clients not present in this batch.
     *
     * @param clients
     * @param removeNotPresent
     * @param lazy if false, connectors are reinitialized even if configuration didnt change
     * @throws IOException
     */
    public void resync(Collection<Client> clients, boolean removeNotPresent, boolean lazy) throws IOException{
        final Set<String> newClientIDs = clients.stream()
                .map(Client::getClientId)
                .collect(Collectors.toSet());

        final Set<String> removedClientIDs = forwarders.keySet().stream()
                .filter(e -> !newClientIDs.contains(e))
                .collect(Collectors.toSet());

        // Resync client by client. No optimization on this level.
        for(Client cl : clients){
            this.resync(cl, lazy);
        }

        // Unregister removed clients, shutdown their handlers.
        for(String clId : removedClientIDs){
            final Forwarder fwder = forwarders.remove(clId);
            fwder.unregister(uoidMap);
            fwder.shutdown();
        }
    }

    /**
     * Resync forwarder mappings with given record.
     * @param client client record to reload
     * @param lazy if false, connectors are reinitialized even if configuration didnt change
     */
    public void resync(Client client, boolean lazy) throws IOException {
        final String clientId = client.getClientId();
        final Forwarder forwarder = forwarders.get(clientId);
        if (forwarder == null) {
            final Forwarder newForwarder = newForwarder();
            newForwarder.init(client, uoidMap);

            forwarders.put(clientId, newForwarder);
            return;
        }

        forwarder.resync(client, uoidMap, lazy);
    }

    /**
     * Forwards message to appropriate place.
     * @param msg message to forward
     */
    public void forward(JSONObject msg){
        try {
            final int uoid = RouterImpl.getUserObject(msg);
            final Forwarder forwarder = uoidMap.get(uoid);
            if (forwarder == null){
                return;
            }

            forwarder.forward(msg);

        }catch(JSONException e){
            // if exception do not process
        }
    }

    public void flush(){
        forwarders.entrySet().forEach(e -> e.getValue().flush());
    }

    public void shutdown(){
        forwarders.entrySet().forEach(e -> e.getValue().shutdown());
    }

    @Lookup
    protected Forwarder newForwarder(){
        return null;
    }
}
