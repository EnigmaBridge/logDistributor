package com.enigmabridge.log.distributor.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Configuration holds all configured access tokens.
 *
 * Created by dusanklinec on 03.08.16.
 */
@ConfigurationProperties(prefix = "auth")
@Configuration
public class AuthConfiguration {
    protected Management management;
    protected Business business;

    public AuthConfiguration() {
    }

    public AuthConfiguration(Management management, Business business) {
        this.management = management;
        this.business = business;
    }

    public Management getManagement() {
        return management;
    }

    public Business getBusiness() {
        return business;
    }

    public void setManagement(Management management) {
        this.management = management;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public List<String> getManagementTokens(){
        return management != null && management.getTokens() != null ?
                Collections.unmodifiableList(management.getTokens()) :
                Collections.emptyList();
    }

    public List<String> getBusinessTokens(){
        return business != null && business.getTokens() != null ?
                Collections.unmodifiableList(business.getTokens()) :
                Collections.emptyList();
    }

    public static class Management {
        private List<String> tokens;

        public Management() {
        }

        public Management(List<String> tokens) {
            this.tokens = tokens;
        }

        public List<String> getTokens() {
            return tokens;
        }

        public void setTokens(List<String> tokens) {
            this.tokens = tokens;
        }
    }

    public static class Business {
        private List<String> tokens;

        public Business() {
        }

        public Business(List<String> tokens) {
            this.tokens = tokens;
        }

        public List<String> getTokens() {
            return tokens;
        }

        public void setTokens(List<String> tokens) {
            this.tokens = tokens;
        }
    }
}
