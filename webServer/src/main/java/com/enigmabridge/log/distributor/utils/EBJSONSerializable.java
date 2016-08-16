package com.enigmabridge.log.distributor.utils;

import org.json.JSONObject;

/**
 * Serializable to JSON object.
 * Created by dusanklinec on 04.05.16.
 */
public interface EBJSONSerializable {
    JSONObject toJSON(JSONObject json);
}
