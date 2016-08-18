package com.enigmabridge.log.distributor;

import com.enigmabridge.log.distributor.db.DbHelper;
import com.enigmabridge.log.distributor.db.model.EBHost;
import com.enigmabridge.log.distributor.forwarder.Router;
import com.enigmabridge.log.distributor.rest.LogicManager;
import com.enigmabridge.log.distributor.utils.EBConnector;
import com.enigmabridge.log.distributor.utils.EBRawRequest;
import com.enigmabridge.log.distributor.utils.EBRawResponse;
import com.enigmabridge.retry.EBUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * Created by dusanklinec on 11.08.16.
 */
@Component
public class Server {
    private final static Logger LOG = LoggerFactory.getLogger(Server.class);

    @Autowired
    private Router router;

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private LogicManager logic;

    public void reload(boolean lazy){
        router.reload(lazy);
    }

    /**
     * Resynchronizes configured hosts records.
     */
    @Async(value = Application.SERVER_RESYNC_EXECUTOR)
    @Scheduled(initialDelay = 20000, fixedRate = 1000*60*10)
    @Transactional
    public void resyncHostsTask() throws IOException {
        resyncHostsInternal();
    }

    protected void resyncHostsInternal() throws IOException {
        final Iterable<EBHost> allHosts = dbHelper.findAllHosts();
        for (EBHost host : allHosts) {
            if (Boolean.TRUE.equals(host.getSupportsSync())) {
                resyncHost(host);
            }
        }
    }

    protected void resyncHost(EBHost host) throws IOException {
        final String url = "http://" + host.getHostAddress() + ":12000/1.0/testAPI/GetAllAPIKeys/" + System.currentTimeMillis();
        try {
            final EBRawRequest req = new EBRawRequest();
            req.setUrl(url);

            final EBConnector conn = new EBConnector();
            conn.setRawRequest(req);
            final EBRawResponse resp = conn.request();
            if (!resp.isSuccessful()){
                LOG.warn("Request unsuccessful: {}", url);
                return;
            }

            final String body = resp.getBody();
            final JSONObject obj  = new JSONObject(body);
            if (obj == null || !obj.has("status")){
                LOG.warn("Request has invalid status url: {} body: {}", url, body);
                return;
            }

            final Integer status = EBUtils.tryGetAsInteger(obj, "status", 16);
            if (status == null || status.intValue() != 0x9000){
                LOG.warn("Request has invalid status url: {} status: {}", url, status);
                return;
            }

            logic.processSiteDump(body);

        } catch(Exception e){
            LOG.error("Exception in syncing host: " + url, e);
        }
    }


}
