package com.enigmabridge.log.distributor.forwarder.splunk;

import com.enigmabridge.log.distributor.forwarder.IClientForwarder;
import org.json.JSONObject;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by dusanklinec on 08.08.16.
 */
public class EBSplunkHandler implements IClientForwarder{
    protected HttpEventCollectorSender sender = null;

    protected String token;
    protected String url;
    protected long delay = HttpEventCollectorSender.DefaultBatchInterval;
    protected long batchCount = HttpEventCollectorSender.DefaultBatchCount;
    protected long batchSize = HttpEventCollectorSender.DefaultBatchSize;
    protected long retriesOnError = 4;
    protected String sendMode = "sequential";
    protected boolean disableCertificateValidation = false;

    public static abstract class AbstractBuilder<T extends EBSplunkHandler, B extends EBSplunkHandler.AbstractBuilder> {
        public B setToken(String token) {
            getObj().setToken(token);
            return getThisBuilder();
        }

        public B setUrl(String url) {
            getObj().setUrl(url);
            return getThisBuilder();
        }

        public B setDelay(long delay) {
            getObj().setDelay(delay);
            return getThisBuilder();
        }

        public B setBatchCount(long batchCount) {
            getObj().setBatchCount(batchCount);
            return getThisBuilder();
        }

        public B setBatchSize(long batchSize) {
            getObj().setBatchSize(batchSize);
            return getThisBuilder();
        }

        public B setRetriesOnError(long retriesOnError) {
            getObj().setRetriesOnError(retriesOnError);
            return getThisBuilder();
        }

        public B setSendMode(String sendMode) {
            getObj().setSendMode(sendMode);
            return getThisBuilder();
        }

        public B setDisableCertificateValidation(boolean disableCertificateValidation) {
            getObj().setDisableCertificateValidation(disableCertificateValidation);
            return getThisBuilder();
        }

        public abstract T build();
        public abstract B getThisBuilder();
        public abstract T getObj();
    }

    public static class Builder extends AbstractBuilder<EBSplunkHandler, EBSplunkHandler.Builder> {
        private final EBSplunkHandler child = new EBSplunkHandler();

        @Override
        public EBSplunkHandler getObj() {
            return child;
        }

        @Override
        public EBSplunkHandler build() {
            return child;
        }

        @Override
        public Builder getThisBuilder() {
            return this;
        }
    }

    /** HttpEventCollectorLoggingHandler c-or */
    public EBSplunkHandler() {

    }

    protected void init(){
        // read configuration settings
        Dictionary<String, String> metadata = new Hashtable<String, String>();
//        metadata.put(HttpEventCollectorSender.MetadataIndexTag,
//                getConfigurationProperty(HttpEventCollectorSender.MetadataIndexTag, ""));
//
//        metadata.put(HttpEventCollectorSender.MetadataSourceTag,
//                getConfigurationProperty(HttpEventCollectorSender.MetadataSourceTag, ""));
//
//        metadata.put(HttpEventCollectorSender.MetadataSourceTypeTag,
//                getConfigurationProperty(HttpEventCollectorSender.MetadataSourceTypeTag, ""));

        // delegate all configuration params to event sender
        this.sender = new HttpEventCollectorSender(
                url, token, delay, batchCount, batchSize, sendMode, metadata);

        // plug retries middleware
        if (retriesOnError > 0) {
            this.sender.addMiddleware(new HttpEventCollectorResendMiddleware(retriesOnError));
        }

        if (disableCertificateValidation) {
            this.sender.disableCertificateValidation();
        }
    }

    /**
     * java.util.logging data handler callback
     * @param msg is a logging record
     */
    public synchronized void publish(JSONObject msg) {
        this.sender.send("", msg.toString());
    }

    /**
     * java.util.logging data handler callback
     */
    public synchronized void flush() {
        this.sender.flush();
    }

    /**
     * shutdown forwarder
     */
    public synchronized void shutdown() {
        this.sender.close();
    }

    protected void setToken(String token) {
        this.token = token;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected void setDelay(long delay) {
        this.delay = delay;
    }

    protected void setBatchCount(long batchCount) {
        this.batchCount = batchCount;
    }

    protected void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }

    protected void setRetriesOnError(long retriesOnError) {
        this.retriesOnError = retriesOnError;
    }

    protected void setSendMode(String sendMode) {
        this.sendMode = sendMode;
    }

    protected void setDisableCertificateValidation(boolean disableCertificateValidation) {
        this.disableCertificateValidation = disableCertificateValidation;
    }
}
