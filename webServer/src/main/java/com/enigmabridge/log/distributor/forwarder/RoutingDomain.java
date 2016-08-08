package com.enigmabridge.log.distributor.forwarder;

import com.enigmabridge.log.distributor.db.model.Client;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
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
     * Resync forwarder mappings with given record.
     * @param client client record to reload
     */
    public void resync(Client client){
        final String clientId = client.getClientId();
        final Forwarder forwarder = forwarders.get(clientId);
        if (forwarder == null) {
            final Forwarder newForwarder = newForwarder();
            newForwarder.init(client, uoidMap);
            newForwarder.start();

            forwarders.put(clientId, newForwarder);
            return;
        }

        forwarder.resync(client, uoidMap);
    }

    /**
     * Forwards message to appropriate place.
     * @param msg message to forward
     */
    public void forward(JSONObject msg){
        try {
            final int uoid = Router.getUserObject(msg);
            final Forwarder forwarder = uoidMap.get(uoid);
            if (forwarder == null){
                return;
            }

            forwarder.forward(msg);

        }catch(JSONException e){
            // if exception do not process
        }
    }

    public void shutdown(){
        forwarders.entrySet().forEach(e -> e.getValue().shutdown());
    }

    @Lookup
    protected Forwarder newForwarder(){
        return null;
    }
}
