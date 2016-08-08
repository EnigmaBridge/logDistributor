package com.enigmabridge.log.distributor.listener;

import com.enigmabridge.log.distributor.Utils;
import com.enigmabridge.log.distributor.api.ApiConfig;
import com.enigmabridge.log.distributor.forwarder.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Log collector.
 * Logstash is connected to this endpoint.
 *
 * Created by dusanklinec on 05.08.16.
 */
@Service
@DependsOn(value = ApiConfig.YAML_CONFIG)
public class LogInputListener extends Thread {
    private final static Logger LOG = LoggerFactory.getLogger(LogInputListener.class);
    public final static String DEFAULT_HOST = "localhost";
    public final static int DEFAULT_BACKLOG = 32;

    @Value("${listener.host}")
    protected String listenIp = "localhost";

    @Value("${listener.port}")
    protected int listenPort = 8999;

    /**
     * Server is running flag.
     */
    protected volatile boolean running = true;

    @PostConstruct
    public void init(){
        start();
    }

    @PreDestroy
    public void deinit(){
        shutdownListener();
    }

    @Override
    public void run() {
        // Start the server.
        ServerSocket listenSocket = null;
        try {
            listenSocket = (listenIp.equals(DEFAULT_HOST)) ?
                    new ServerSocket(listenPort) :
                    new ServerSocket(listenPort, DEFAULT_BACKLOG, InetAddress.getByName(listenIp));
            listenSocket.setSoTimeout(3000);
            LOG.info("Server is listening on: {}:{}", listenIp, listenPort);

            // Listening loop.
            while (running) {
                try {
                    final Socket clientSocket = listenSocket.accept();
                    LOG.debug("User connected: {}", clientSocket.getRemoteSocketAddress());

                    // Start new connection socket.
                    final LogInputProcessor c = newProcessor();
                    c.init(this, clientSocket);
                    c.start();

                } catch (SocketTimeoutException timeout) {
                    // Timeout is OK.
                }
            }

            LOG.info("Server shutting down");
        } catch (Exception e) {
            LOG.error("Listen exception", e);
        } finally {
            Utils.closeSilently(listenSocket);
        }
    }

    @Lookup
    public LogInputProcessor newProcessor(){
        //spring will override this method
        return null;
    }

    public void shutdownListener() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public String getListenIp() {
        return listenIp;
    }

    public int getListenPort() {
        return listenPort;
    }
}
