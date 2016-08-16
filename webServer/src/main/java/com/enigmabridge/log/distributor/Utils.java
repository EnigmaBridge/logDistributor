package com.enigmabridge.log.distributor;

import org.hjson.JsonValue;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.io.Closeable;
import java.util.Map;
import java.util.Optional;

/**
 * Created by dusanklinec on 31.07.16.
 */
public class Utils {
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String byte2hexNullable(byte[] bytes){
        if (bytes == null){
            return "";
        }

        return DatatypeConverter.printHexBinary(bytes);
    }

    public static <K1, K2, T> T getMap(Map<K1, Map<K2, T>> map, K1 k1, K2 k2){
        if (map == null){
            return null;
        }
        final Map<K2, T> k2TMap = map.get(k1);
        if (k2TMap == null){
            return null;
        }

        return k2TMap.get(k2);
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

    /**
     * Tries to extract json parameter as an integer.
     * @param json target
     * @param key field name
     * @return extracted boolean
     * @throws JSONException
     */
    public static Boolean tryGetAsBoolean(JSONObject json, String key) throws JSONException {
        final Object obj = json.get(key);
        if (obj == null){
            return null;
        }

        if(!obj.equals(Boolean.FALSE) && (!(obj instanceof String) || !((String)obj).equalsIgnoreCase("false"))) {
            if(!obj.equals(Boolean.TRUE) && (!(obj instanceof String) || !((String)obj).equalsIgnoreCase("true"))) {
                final Integer asInt = tryGetAsInteger(json, key, 10);
                if (asInt == null){
                    return null;
                }

                return asInt!=0;

            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Tries to extract json parameter as a string.
     * If parameter is not present or is not a string, null is returned.
     *
     * @param json target
     * @param key field name
     * @return extracted string
     */
    public static Optional<JSONObject> getAsJSON(JSONObject json, String key) {
        if (!json.has(key)){
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(json.getJSONObject(key));
        } catch(JSONException e){
            return Optional.empty();
        }
    }

    /**
     * Tries to extract json parameter as a string.
     * If parameter is not present or is not a string, null is returned.
     *
     * @param json target
     * @param key field name
     * @return extracted string
     */
    public static Optional<String> getAsString(JSONObject json, String key) {
        if (!json.has(key)){
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(json.getString(key));
        } catch(JSONException e){
            return Optional.empty();
        }
    }

    /**
     * Tries to extract json parameter as an string.
     * @param json target
     * @param key field name
     * @return extracted string
     * @throws JSONException - if the JSON object doesn't contain the item or is malformed
     */
    public static String tryGetAsString(JSONObject json, String key) throws JSONException {
        return json.getString(key);
    }

    /**
     * Tries to extract json parameter as an integer.
     * @param json target
     * @param key field name
     * @param radix radix for string / int conversion
     * @return extracted integer
     * @throws JSONException - if the JSON object doesn't contain the item or is malformed
     */
    public static Integer tryGetAsInteger(JSONObject json, String key, int radix) throws JSONException {
        final Object obj = json.get(key);

        if (obj instanceof String){
            try {
                return Integer.parseInt((String) obj, radix);
            } catch(Exception e){
                return null;
            }
        }

        try {
            return obj instanceof Number ? ((Number) obj).intValue() : (int) json.getDouble(key);
        } catch(Exception e){
            return null;
        }
    }

    /**
     * Tries to extract json parameter as a long.
     * @param json target
     * @param key field name
     * @param radix radix for string / int conversion
     * @return extracted long
     * @throws JSONException - if the JSON object doesn't contain the item or is malformed
     */
    public static Long tryGetAsLong(JSONObject json, String key, int radix) throws JSONException {
        final Object obj = json.get(key);

        if (obj instanceof String){
            try {
                return Long.parseLong((String) obj, radix);
            } catch(Exception e){
                return null;
            }
        }

        try {
            return obj instanceof Number ? ((Number) obj).longValue() : (long) json.getDouble(key);
        } catch(Exception e){
            return null;
        }
    }

    public static long getAsLong(JSONObject json, String key, int radix) throws JSONException {
        final Long toret = tryGetAsLong(json, key, radix);
        if (toret == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }

        return toret;
    }

    public static int getAsInteger(JSONObject json, String key, int radix) throws JSONException {
        final Integer toret = tryGetAsInteger(json, key, radix);
        if (toret == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }

        return toret;
    }

    public static boolean getAsBoolean(JSONObject json, String key) throws JSONException {
        final Boolean toret = tryGetAsBoolean(json, key);
        if (toret == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }

        return toret;
    }
}
