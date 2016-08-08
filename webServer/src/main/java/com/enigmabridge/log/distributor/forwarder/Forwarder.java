package com.enigmabridge.log.distributor.forwarder;

import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.UserObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Created by dusanklinec on 08.08.16.
 */
@Component
@Scope("prototype")
public class Forwarder extends Thread {
    private final static Logger LOG = LoggerFactory.getLogger(Forwarder.class);

    /**
     * Client this forwarder is dedicated to.
     */
    protected Client client;

    /**
     * Queue of messages to forward
     */
    protected final ConcurrentLinkedQueue<JSONObject> msgQueue = new ConcurrentLinkedQueue<>();

    /**
     * Connection to the client
     */
    protected Socket connection;

    protected volatile boolean running = true;

    /**
     * Initializes forwarder with given client
     * @param client
     * @param uoidMap to register to
     */
    public void init(Client client, Map<Integer, Forwarder> uoidMap){
        // TODO: implement
        this.client = client;
        resync(client, uoidMap);
    }

    /**
     * Resynchronizes forwarder with the client
     * @param client
     * @param uoidMap to register to
     */
    public void resync(Client client, Map<Integer, Forwarder> uoidMap){
        final Client oldClient = this.client;
        this.client = client;

        // TODO: implement
        // TODO: reconnect if configuration changed.

        checkConnected();

        // Init -> just add quickly.
        if (this.client == oldClient){
            final Map<Integer, Forwarder> addMap = getAddMap();
            uoidMap.putAll(addMap);
            return;
        }

        // Resync:
        // Re-register ouid mapping - get removed & added records.
        final Set<Integer> newUOs = this.client.getObjects()
                .stream()
                .map(UserObject::getUoId)
                .collect(Collectors.toSet());

        final Set<Integer> oldUOs = this.client.getObjects()
                .stream()
                .map(UserObject::getUoId)
                .collect(Collectors.toSet());

        final List<Integer> removedUOs = oldUOs.stream()
                .filter(uo -> !newUOs.contains(uo))
                .collect(Collectors.toList());

        final Map<Integer, Forwarder> addMap = newUOs.stream()
                .filter(uo -> !oldUOs.contains(uo))
                .collect(Collectors.toMap(uo -> uo, uo -> this));

        for(Integer removedUo : removedUOs){
            uoidMap.remove(removedUo);
        }

        uoidMap.putAll(addMap);
    }

    protected Map<Integer, Forwarder> getAddMap(){
        return client.getObjects().stream()
                .map(UserObject::getUoId)
                .collect(Collectors.toMap(e -> e, e -> this));
    }

    /**
     * Enqueues given message for forwarding.
     * @param msg log message to enqueue
     */
    public void forward(JSONObject msg){
        if (!isForwardingEnabled()){
            return;
        }

        msgQueue.add(msg);
    }

    public boolean isForwardingEnabled(){
        return running && client != null && client.getSplunkConfig() != null;
    }

    public void shutdown(){
        this.running = false;
    }

    protected void checkConnected(){
        // TODO: implement
    }

    @Override
    public void run() {
        LOG.info("Forwarder started");
        while(running){
            try {
                // TODO: connect & consume queue with messages
                checkConnected();

                Thread.yield();
            } catch (Exception e){
                LOG.error("Exception in forwarder", e);
            }
        }
    }
}
