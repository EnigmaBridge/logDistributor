package com.enigmabridge.log.distributor.utils;

import com.enigmabridge.log.distributor.LogConstants;
import com.enigmabridge.log.distributor.Utils;
import com.enigmabridge.log.distributor.db.model.Client;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Created by dusanklinec on 16.08.16.
 */
public class DomainUtils {
    public static String getDomain(Client client){
        final String clDomain = client.getDomain().getDomain();
        return clDomain == null || clDomain.isEmpty() ? LogConstants.DEFAULT_DOMAIN : clDomain.toLowerCase();
    }

    public static String sanitize(String domain){
        if (domain == null || domain.isEmpty()){
            return LogConstants.DEFAULT_DOMAIN;
        }

        return domain.toLowerCase();
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
