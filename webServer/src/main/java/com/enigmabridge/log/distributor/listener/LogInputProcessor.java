package com.enigmabridge.log.distributor.listener;

import com.enigmabridge.log.distributor.LogConstants;
import com.enigmabridge.log.distributor.Stats;
import com.enigmabridge.log.distributor.forwarder.Router;
import com.enigmabridge.log.distributor.utils.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Future;

/**
 * Reads socket and processes log records.
 *
 * Example logs:
 * {@literal
 *     {"time":"2016-07-31 20:35:43,538", "code": "SHC0009USE", "version": "1.0", "user": "user", "ip": "/193.60.89.226", "server": "damselfly.ideaspace.cam.ac.uk", "details": {"cmd":"GETUSEROBJECT_XC","source":"n\/a","seedcard":6,"controlcard":3,"latency":146,"userobject":1,"operation":0,"sw":"9000"}, "unumber":1 }
 *     {"time":"2016-07-31 20:35:55,459", "code": "SHC0001USE", "version": "1.0", "user": "user", "ip": "/193.60.89.226", "server": "damselfly.ideaspace.cam.ac.uk", "details": {"cmd":"PROCESS","source":"n\/a","cryptocard":8,"latency":731,"userobject":1,"operation":0,"sw":"9000"}, "unumber":12 }
 * }
 *
 * Created by dusanklinec on 05.08.16.
 */
@Component
@Scope("prototype")
public class LogInputProcessor extends Thread {
    private final static Logger LOG = LoggerFactory.getLogger(LogInputProcessor.class);

    protected InputStream input;
    protected BufferedReader bufferedInput;
    protected OutputStream output;
    protected Socket clientSocket;
    protected LogInputListener parent;

    protected PrintStream printOut;
    protected String inputData;

    @Autowired
    protected Router router;

    @Autowired
    private Stats stats;

    public LogInputProcessor() {
    }

    public LogInputProcessor(LogInputListener parent, Socket aClientSocket) {
        init(parent, aClientSocket);
    }

    public void init(LogInputListener parent, Socket aClientSocket){
        try {
            clientSocket = aClientSocket;
            input = clientSocket.getInputStream();
            bufferedInput = new BufferedReader(new InputStreamReader(input));
            output = clientSocket.getOutputStream();
            this.parent = parent;

        } catch (IOException e) {
            LOG.error("Exception", e);
        }
    }

    public void run() {
        printOut = new PrintStream(output);
        Future<?> future = null;

        try {
            // Read lines from the connection.
            for(;parent.isRunning() && !clientSocket.isClosed() && !clientSocket.isInputShutdown();){
                final String jsonLine = bufferedInput.readLine();
                if (jsonLine == null){
                    stats.incTcpClosed();
                    break;
                }

                stats.incLogLinesProcessed();
                try {
                    final JSONObject jsonObject = Utils.parseJSON(jsonLine);
                    if (!jsonObject.has(LogConstants.FIELD_DETAILS)){
                        continue;
                    }

                    final Object detailsObj = jsonObject.get(LogConstants.FIELD_DETAILS);
                    if (!(detailsObj instanceof JSONObject)){
                        continue;
                    }

                    final JSONObject details = (JSONObject) detailsObj;
                    if (!details.has(LogConstants.FIELD_CMD)){
                        continue;
                    }

                    final Object cmdObj = details.get(LogConstants.FIELD_CMD);
                    if (cmdObj == null || !(cmdObj instanceof String)){
                        continue;
                    }

                    if (!LogConstants.FIELD_PROCESS.equalsIgnoreCase((String) cmdObj)){
                        continue;
                    }

                    router.processMessage(jsonObject);

                } catch(Exception e){
                    LOG.warn("Exception in parsing JSON line", e);
                }
            }

        } catch (Exception e) {
            printOut.println("Exception: " + e.getMessage());

        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                    /*close failed*/
            }
        }
    }

    public PrintStream getPrintOut() {
        return printOut;
    }

    public String getInputData() {
        return inputData;
    }
}
