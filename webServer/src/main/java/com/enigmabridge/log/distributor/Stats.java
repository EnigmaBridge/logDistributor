package com.enigmabridge.log.distributor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dusanklinec on 18.08.16.
 */
@Component
public class Stats {
    private final AtomicLong numReloads = new AtomicLong(0);
    private final AtomicLong numFlushes = new AtomicLong(0);
    private final AtomicLong numHostResync = new AtomicLong(0);
    private final AtomicLong numHostResyncFailed = new AtomicLong(0);
    private final AtomicLong numUOAdded = new AtomicLong(0);
    private final AtomicLong numUORemoved = new AtomicLong(0);
    private final AtomicLong numTcpConnections = new AtomicLong(0);
    private final AtomicLong numTcpClosed = new AtomicLong(0);
    private final AtomicLong numTcpCurrent = new AtomicLong(0);
    private final AtomicLong numLogLinesProcessed = new AtomicLong(0);
    private final AtomicLong numLogLinesForwarded = new AtomicLong(0);

    private long lastHostResync = 0;
    private long lastTcpConnection = 0;
    private long lastTcpClosed = 0;
    private long lastLoglineProcessed = 0;
    private long lastLoglineForwarded = 0;

    public void incReloads(){
        numReloads.incrementAndGet();
    }
    public void incFlushes(){
        numFlushes.incrementAndGet();
    }
    public void incHostResync(){
        numHostResync.incrementAndGet();
        lastHostResync = System.currentTimeMillis();
    }
    public void incHostResyncFailed(){
        numHostResyncFailed.incrementAndGet();
        lastHostResync = System.currentTimeMillis();
    }
    public void incUOAdded(int added){
        numUOAdded.addAndGet(added);
    }
    public void incUORemoved(int removed){
        numUORemoved.addAndGet(removed);
    }
    public void incTcpConnections(){
        numTcpConnections.incrementAndGet();
        numTcpCurrent.incrementAndGet();
        lastTcpConnection = System.currentTimeMillis();
    }
    public void incTcpClosed(){
        numTcpClosed.incrementAndGet();
        numTcpCurrent.decrementAndGet();
        lastTcpClosed = System.currentTimeMillis();
    }
    public void incLogLinesProcessed(){
        numLogLinesProcessed.incrementAndGet();
        lastLoglineProcessed = System.currentTimeMillis();
    }
    public void incLogLinesForwarded(){
        numLogLinesForwarded.incrementAndGet();
        lastLoglineForwarded = System.currentTimeMillis();
    }



    public AtomicLong getNumReloads() {
        return numReloads;
    }

    public AtomicLong getNumFlushes() {
        return numFlushes;
    }

    public AtomicLong getNumHostResync() {
        return numHostResync;
    }

    public AtomicLong getNumHostResyncFailed() {
        return numHostResyncFailed;
    }

    public AtomicLong getNumUOAdded() {
        return numUOAdded;
    }

    public AtomicLong getNumUORemoved() {
        return numUORemoved;
    }

    public long getLastHostResync() {
        return lastHostResync;
    }

    public AtomicLong getNumTcpConnections() {
        return numTcpConnections;
    }

    public AtomicLong getNumTcpClosed() {
        return numTcpClosed;
    }

    public AtomicLong getNumTcpCurrent() {
        return numTcpCurrent;
    }

    public AtomicLong getNumLogLinesProcessed() {
        return numLogLinesProcessed;
    }

    public AtomicLong getNumLogLinesForwarded() {
        return numLogLinesForwarded;
    }

    public long getLastTcpConnection() {
        return lastTcpConnection;
    }

    public long getLastTcpClosed() {
        return lastTcpClosed;
    }

    public long getLastLoglineProcessed() {
        return lastLoglineProcessed;
    }

    public long getLastLoglineForwarded() {
        return lastLoglineForwarded;
    }
}
