package com.enigmabridge.log.distributor.db;

import com.enigmabridge.log.distributor.api.requests.ClientReq;
import com.enigmabridge.log.distributor.api.requests.LogstashConfigReq;
import com.enigmabridge.log.distributor.api.requests.SplunkConfigReq;
import com.enigmabridge.log.distributor.api.requests.UserObjectReq;
import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.LogstashConfig;
import com.enigmabridge.log.distributor.db.model.SplunkConfig;
import com.enigmabridge.log.distributor.db.model.UserObject;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Converts & sanitizes request to DB entities.
 *
 * Created by dusanklinec on 04.08.16.
 */
@Component
public class ClientBuilder {
    public Client build(ClientReq req){
        final Client cl = new Client();

        cl.setClientId(req.getClientId());
        cl.setDomain(req.getDomain());

        if (req.getLogstashConfig() != null){
            cl.setLogstashConfig(build(req.getLogstashConfig()));
        }

        if (req.getSplunkConfig() != null){
            cl.setSplunkConfig(build(req.getSplunkConfig()));
        }

        final List<UserObjectReq> objects = req.getObjects();
        if (objects != null && !objects.isEmpty()){
            for (UserObjectReq object : objects) {
                cl.addObject(build(object));
            }
        }

        return cl;
    }

    public LogstashConfig build(LogstashConfigReq req){
        if (req == null){
            return null;
        }

        final LogstashConfig ret = new LogstashConfig();
        return ret;
    }

    public SplunkConfig build(SplunkConfigReq req){
        if (req == null){
            return null;
        }

        final SplunkConfig ret = new SplunkConfig();
        if (req.getToken() != null){
            ret.setToken(req.getToken());
        }

        if (req.getEndpoint() != null){
            ret.setEndpoint(req.getEndpoint());
        }

        if (req.getCaRoots() != null){
            ret.setCaRoots(req.getCaRoots());
        }

        if (req.getVerifyTls() != null){
            ret.setVerifyTls(req.getVerifyTls());
        }
        return ret;
    }

    public UserObject build(UserObjectReq req){
        if (req == null){
            return null;
        }

        final UserObject ret = new UserObject();
        if (req.getApiKey() != null){
            ret.setApiKey(req.getApiKey());
        }

        if (req.getUoId() != null){
            ret.setUoId(req.getUoId());
        }

        if (req.getUoType() != null){
            ret.setUoType(req.getUoType());
        }
        return ret;
    }
}
