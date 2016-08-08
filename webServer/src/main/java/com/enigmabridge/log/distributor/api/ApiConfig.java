package com.enigmabridge.log.distributor.api;

/**
 * Service configuration strings.
 *
 * Created by dusanklinec on 01.08.16.
 */
public class ApiConfig {
    public static final String API_PATH = "/api/v1";

    public static final String AUTHENTICATE_URL = API_PATH + "/authenticate";
    public static final String UO_EVENT_URL = API_PATH + "/evt/uo";

    // Spring Boot Actuator services
    public static final String AUTOCONFIG_ENDPOINT = "/autoconfig";
    public static final String BEANS_ENDPOINT = "/beans";
    public static final String CONFIGPROPS_ENDPOINT = "/configprops";
    public static final String ENV_ENDPOINT = "/env";
    public static final String MAPPINGS_ENDPOINT = "/mappings";
    public static final String METRICS_ENDPOINT = "/metrics";
    public static final String SHUTDOWN_ENDPOINT = "/shutdown";

    public static final String MANAGEMENT_ROLE_SUFFIX = "BACKEND_ADMIN";
    public static final String MANAGEMENT_ROLE = "ROLE_" + MANAGEMENT_ROLE_SUFFIX;

    public static final String BUSINESS_ROLE_SUFIX = "BUSINESS_ADMIN";
    public static final String BUSINESS_ROLE = "ROLE_" + BUSINESS_ROLE_SUFIX;

    public static final String YAML_CONFIG = "yaml-config";
    public static final String ROUTER = "router";
}
