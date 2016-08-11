package com.enigmabridge.log.distributor;

import com.enigmabridge.log.distributor.forwarder.Router;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * Created by dusanklinec on 11.08.16.
 */
@Component
public class Server {
    @Autowired
    private Router router;

    public void reload(boolean lazy){
        router.reload(lazy);
    }
}
