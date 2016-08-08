package com.enigmabridge.log.distributor.forwarder;

import com.enigmabridge.log.distributor.LogConstants;
import com.enigmabridge.log.distributor.Utils;
import com.enigmabridge.log.distributor.api.ApiConfig;
import com.enigmabridge.log.distributor.listener.LogInputProcessor;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dusanklinec on 08.08.16.
 */
@Component
@DependsOn(value = ApiConfig.YAML_CONFIG)
public class Router {
    private final static Logger LOG = LoggerFactory.getLogger(Router.class);

    /**
     * Domain -> domain routing configuration.
     */
    protected final Map<String, RoutingDomain> domains = new ConcurrentHashMap<>(8);

    @PostConstruct
    public void init(){
        reload();
    }

    @Async
    public void reload(){
        LOG.info("Reloading router...");
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


        } catch(JSONException e){
            // Not a log message worth forwarding
        }
    }

    protected int getUserObject(JSONObject msg){
        return Utils.getAsInteger(msg.getJSONObject(LogConstants.FIELD_DETAILS), LogConstants.FIELD_UO, 10);
    }

    protected String getDomain(JSONObject msg){
        return getDomain(msg.getString(LogConstants.FIELD_SERVER));
    }

    protected String getDomain(String server){
        final int separatorIdx = server.indexOf("_");
        if (separatorIdx == -1){
            return LogConstants.DEFAULT_DOMAIN;
        }

        return server.substring(0, separatorIdx).toLowerCase();
    }
}
