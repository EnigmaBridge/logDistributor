package com.enigmabridge.log.distributor.forwarder;

import org.json.JSONObject;

import java.util.concurrent.Future;

/**
 * Because of aspects we need interface here so proxy can be substituted.
 *
 * Created by dusanklinec on 08.08.16.
 */
public interface Router {
    void processMessage(JSONObject jsonObject);
    Future<Integer> reload();
    void shutdown();
}
