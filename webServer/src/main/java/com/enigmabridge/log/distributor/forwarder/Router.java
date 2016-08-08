package com.enigmabridge.log.distributor.forwarder;

import com.enigmabridge.log.distributor.db.model.Client;
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
    Future<Integer> reload(Iterable<Client> clients);

    void shutdown();
}
