package com.enigmabridge.log.distributor;

import org.hjson.JsonValue;
import org.json.JSONObject;

import java.io.Closeable;

/**
 * Created by dusanklinec on 31.07.16.
 */
public class Utils {
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static JSONObject parseJSON(String json){
        return new JSONObject(JsonValue.readHjson(json).toString());
    }

    public static void closeSilently(Closeable c){
        if (c == null){
            return;
        }

        try {
            c.close();
        } catch(Exception e){

        }
    }
}
