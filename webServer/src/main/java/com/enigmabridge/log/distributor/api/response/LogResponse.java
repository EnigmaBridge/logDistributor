package com.enigmabridge.log.distributor.api.response;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dusanklinec on 11.08.16.
 */
public class LogResponse extends ResultResponse {
    protected final List<String> lines = new LinkedList<>();

    public List<String> getLines() {
        return lines;
    }

    public void addLine(String line){
        lines.add(line);
    }
}
