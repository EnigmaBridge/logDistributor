package com.enigmabridge.log.distributor.forwarder;

import org.json.JSONObject;

/**
 * Created by dusanklinec on 08.08.16.
 */
public interface IClientForwarder {
    /**
     * java.util.logging data handler callback
     * @param msg is a logging record
     */
    void publish(JSONObject msg);

    /**
     * Flush buffered
     */
    void flush();

    /**
     * Forwarder shutdown.
     */
    void shutdown();
}
