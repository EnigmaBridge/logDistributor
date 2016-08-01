package com.enigmabridge.logDistributor.config;

import java.io.*;

import com.enigmabridge.logDistributor.Utils;
import org.json.JSONObject;

/**
 * Created by dusanklinec on 31.07.16.
 */
public class Configuration {
    protected final String FIELD_DB = "sqlite";

    protected String dbFile;

    public Configuration(){

    }

    public Configuration(String filePath) throws IOException {
        final FileInputStream fis = new FileInputStream(new File(filePath));
        initFromJson(Utils.parseJSON(Utils.convertStreamToString(fis)));
        fis.close();
    }

    public Configuration(File file) throws IOException {
        final FileInputStream fis = new FileInputStream(file);
        initFromJson(Utils.parseJSON(Utils.convertStreamToString(fis)));
        fis.close();
    }

    public Configuration(InputStream in){
        initFromJson(Utils.parseJSON(Utils.convertStreamToString(in)));
    }

    public Configuration(JSONObject json){
        initFromJson(json);
    }

    protected void initFromJson(JSONObject json){
        if (json.has(FIELD_DB)){
            dbFile = json.getString(FIELD_DB);
        }

    }

    public String getDbFile() {
        return dbFile;
    }


}
