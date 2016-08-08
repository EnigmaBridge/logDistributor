package com.enigmabridge.log.distributor.forwarder;

import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.LogstashConfig;
import com.enigmabridge.log.distributor.db.model.SplunkConfig;
import com.enigmabridge.log.distributor.db.model.UserObject;
import com.enigmabridge.log.distributor.forwarder.splunk.EBSplunkHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by dusanklinec on 08.08.16.
 */
@Component
@Scope("prototype")
public class Forwarder {
    private final static Logger LOG = LoggerFactory.getLogger(Forwarder.class);

    /**
     * Client this forwarder is dedicated to.
     */
    protected Client client;

    /**
     * Underlying forwarder instance
     */
    protected IClientForwarder clientForwarder;

    protected volatile boolean running = true;

    /**
     * Initializes forwarder with given client
     * @param client client to configure
     * @param uoidMap to register to
     */
    public void init(Client client, Map<Integer, Forwarder> uoidMap) throws MalformedURLException {
        this.client = client;
        resync(client, uoidMap);
    }

    /**
     * Resynchronizes forwarder with the client
     * @param client client to configure
     * @param uoidMap to register to
     */
    public void resync(Client client, Map<Integer, Forwarder> uoidMap) throws MalformedURLException {
        final Client oldClient = this.client;
        this.client = client;

        // Configuration change?
        if (client == oldClient){
            // New configuration.
            this.clientForwarder = buildHandler(client);

        } else if (configurationDiffers(client, oldClient)) {
            if (clientForwarder != null){
                try {
                    clientForwarder.shutdown();
                } catch(Exception e){
                    LOG.error("Exception in closing old forwarder", e);
                }
            }

            this.clientForwarder = buildHandler(client);
        }

        // Init -> just add quickly.
        if (this.client == oldClient){
            final Map<Integer, Forwarder> addMap = getAddMap();
            uoidMap.putAll(addMap);
            return;
        }

        // Resync:
        // Re-register ouid mapping - get removed & added records.
        final Set<Integer> oldUOs = oldClient.getObjects()
                .stream()
                .map(UserObject::getUoId)
                .collect(Collectors.toSet());

        final Set<Integer> newUOs = this.client.getObjects()
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

    protected boolean configurationDiffers(Client a, Client b){
        if (a == b){
            return false;
        }

        final SplunkConfig splunkA = a.getSplunkConfig();
        final SplunkConfig splunkB = b.getSplunkConfig();
        final LogstashConfig logstashA = a.getLogstashConfig();
        final LogstashConfig logstashB = b.getLogstashConfig();

        if (splunkA == null && splunkB == null){
            if (logstashA == null && logstashB == null){
                return false;
            } else if (logstashA != null) {
                return logstashA.equals(logstashB);
            } else {
                return true;
            }
        } else if (splunkA != null){
            return splunkA.equals(splunkB);
        } else {
            return true;
        }
    }

    protected IClientForwarder buildHandler(Client cl) throws MalformedURLException{
        if (cl == null){
            throw new NullPointerException("Client is null");
        }

        final SplunkConfig splunkConfig = cl.getSplunkConfig();
        if (splunkConfig != null){
            return buildHandler(splunkConfig);
        }

        final LogstashConfig logstashConfig = cl.getLogstashConfig();
        if (logstashConfig != null){
            return buildHandler(logstashConfig);
        }

        return null;
    }

    protected IClientForwarder buildHandler(SplunkConfig cfg) throws MalformedURLException {
        final URL url = new URL(cfg.getEndpoint());
        final String protocol = url.getProtocol();
        if (protocol != null && "tcp".equals(protocol)){
            return null; // TODO: implement TCP
        }

        // TODO: caRoots

        return new EBSplunkHandler.Builder()
                .setToken(cfg.getToken())
                .setUrl(cfg.getEndpoint())
                .setBatchCount(10)
                .setDelay(5000)
                .setRetriesOnError(4)
                .setDisableCertificateValidation(!cfg.getVerifyTls())
                .build();
    }

    protected IClientForwarder buildHandler(LogstashConfig cfg){
        return null; // not implemented yet -> no forwarding
    }

    /**
     * Enqueues given message for forwarding.
     * @param msg log message to enqueue
     */
    public void forward(JSONObject msg){
        if (!isForwardingEnabled()){
            return;
        }

        clientForwarder.publish(msg);
    }

    public boolean isForwardingEnabled(){
        return running
                && client != null
                && client.getSplunkConfig() != null
                && clientForwarder != null;
    }

    public void shutdown(){
        this.running = false;
        if (clientForwarder != null) {
            try {
                clientForwarder.flush();
                clientForwarder.shutdown();
            } catch(Exception e){
                LOG.error("Exception when closing forwarder", e);
            }
        }
    }
}
