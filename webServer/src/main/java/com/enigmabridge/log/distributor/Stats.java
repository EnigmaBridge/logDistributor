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
    private final AtomicLong numUOAdded = new AtomicLong(0);
    private final AtomicLong numUORemoved = new AtomicLong(0);
    private long lastHostResync = 0;

    public void incReloads(){
        numReloads.incrementAndGet();
    }
    public void incFlushes(){
        numReloads.incrementAndGet();
    }
    public void incHostResync(){
        numReloads.incrementAndGet();
        lastHostResync = System.currentTimeMillis();
    }
    public void incUOAdded(int added){
        numReloads.addAndGet(added);
    }
    public void incUORemoved(int removed){
        numReloads.addAndGet(removed);
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

    public AtomicLong getNumUOAdded() {
        return numUOAdded;
    }

    public AtomicLong getNumUORemoved() {
        return numUORemoved;
    }

    public long getLastHostResync() {
        return lastHostResync;
    }
}
