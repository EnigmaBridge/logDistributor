package com.enigmabridge.log.distributor.utils;

import com.enigmabridge.retry.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Connector to EB endpoint. It enables to call raw requests on the EB API.
 * Wraps HTTP query library, processes connection settings. Can e.g., perform multiple attempts before failure.
 * Lowest EB level of abstraction for remote calls.
 *
 * In future this may provide outputStream
 *
 * Created by dusanklinec on 26.04.16.
 */
public class EBConnector {
    private static final Logger LOG = LoggerFactory.getLogger(EBConnector.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DEFAULT = METHOD_GET;

    /**
     * Default trust object, with letsencrypt certificates included
     */
    public static final EBAdditionalTrust DEFAULT_TRUST = new EBAdditionalTrust(true, true, (InputStream) null);

    /**
     * Default retry strategy for network related errors (IOExceptions).
     */
    public static final EBRetryStrategy DEFAULT_RETRY = new EBRetryStrategyBackoff.Builder()
            .setMaxElapsedTimeMillis(1000*60*5)
            .build();

    protected EBRawRequest rawRequest;

    /**
     * OkHttp call, for cancellation.
     */
    private Call call;

    /**
     * Do the request, performs real service call.
     *
     * @return EBRawResponse
     * @throws IOException IO
     */
    public EBRawResponse request() throws IOException {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // We are using mainly letsencrypt and if there is no trust object provided, we initialize a default one.
        DEFAULT_TRUST.install(clientBuilder);

        final OkHttpClient client = clientBuilder.build();

        // Take retry strategy from the settings. If not set, use default one.
        final EBRetryStrategy retryStrategy = DEFAULT_RETRY;

        // New retry mechanism instance, for each request.
        final EBRetry<EBRawResponse, Throwable> ebRetry = new EBRetry<EBRawResponse, Throwable>(retryStrategy.copy());

        // Define retry job
        ebRetry.setJob(new EBRetryJobSimpleSafeThrErr<EBRawResponse>() {
            @Override
            public void runAsyncNoException(EBCallback<EBRawResponse, Throwable> callback) throws Throwable {
                try {
                    final EBRawResponse ebRawResponse = requestInternal(client);
                    callback.onSuccess(ebRawResponse);

                } catch(IOException exception) {
                    LOG.debug("EB failed: " + rawRequest.getUrl());
                    callback.onFail(new EBRetryJobErrorThr(exception), false);
                }
            }
        });

        // Start the job synchronously.
        try {
            return ebRetry.runSync();

        } catch (EBRetryFailedException e) {
            throw new IOException(e);

        } catch (EBRetryException e){
            throw new IOException("Fatal request error", e);
        }
    }

    protected EBRawResponse requestInternal(OkHttpClient client) throws IOException {
        final HttpUrl url = HttpUrl.parse(rawRequest.getUrl());

        final Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent", "EBClient.java")
                .addHeader("Accept", "application/json; q=0.5");

        final String method = rawRequest.getMethod();
        if (METHOD_GET.equals(method)){
            // Nothing to do really.

        } else if (METHOD_POST.equals(method)){
            final RequestBody body = RequestBody.create(JSON, rawRequest.getBody());
            requestBuilder.post(body);

        } else {
            throw new IllegalArgumentException("Unknown request method: " + method);
        }

        final Request request = requestBuilder.build();

        // Do the call.
        final long timeStart = System.currentTimeMillis();
        call = client.newCall(request);

        // Synchronous call.
        final Response response = call.execute();
        final ResponseBody body = response.body();
        final byte[] respBytes = body.bytes();
        String respString = null;
        try {
            final Charset charset = body.contentType() == null ? null : body.contentType().charset();
            respString = new String(respBytes, charset == null ? "UTF-8" : charset.name());
        } catch(RuntimeException e){
            LOG.error("Exception in converting response bytes to string", e);
        }

        final EBRawResponse ebResponse = new EBRawResponse();
        ebResponse.setHttpCode(response.code())
                .setBodyBytes(respBytes)
                .setBody(respString)
                .setResponseTime(System.currentTimeMillis() - timeStart)
                .setSuccessful(response.isSuccessful());

        body.close();

        return ebResponse;
    }

    public void cancel(){
        if (call!=null){
            call.cancel();
        }
    }

    public EBRawRequest getRawRequest() {
        return rawRequest;
    }

    public EBConnector setRawRequest(EBRawRequest rawRequest) {
        this.rawRequest = rawRequest;
        return this;
    }
}
