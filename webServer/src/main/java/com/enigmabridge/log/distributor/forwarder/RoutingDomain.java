package com.enigmabridge.log.distributor.forwarder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dusanklinec on 08.08.16.
 */
public class RoutingDomain {
    /**
     * All client forwarders.
     */
    protected final Map<String, Forwarder> forwarders = new ConcurrentHashMap<>();


}
