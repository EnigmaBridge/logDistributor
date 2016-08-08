package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by dusanklinec on 04.08.16.
 */
@Entity
@Table()
public class SplunkConfig {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    /**
     * Contains scheme + IP + port
     * e.g., tcp://192.168.0.1:7777
     */
    @Column
    @NotNull
    private String endpoint;

    /**
     * Auth token for the endpoint
     */
    @Column
    private String token;

    /**
     * String of PEM CA roots. Required for TLS connection.
     */
    @Column
    private String caRoots;

    /**
     * Whether to verify TLS or not.
     */
    @Column
    private Boolean verifyTls = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCaRoots() {
        return caRoots;
    }

    public void setCaRoots(String caRoots) {
        this.caRoots = caRoots;
    }

    public Boolean getVerifyTls() {
        return verifyTls;
    }

    public void setVerifyTls(Boolean verifyTls) {
        this.verifyTls = verifyTls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SplunkConfig that = (SplunkConfig) o;

        if (endpoint != null ? !endpoint.equals(that.endpoint) : that.endpoint != null) return false;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (caRoots != null ? !caRoots.equals(that.caRoots) : that.caRoots != null) return false;
        return verifyTls != null ? verifyTls.equals(that.verifyTls) : that.verifyTls == null;

    }

    @Override
    public int hashCode() {
        int result = endpoint != null ? endpoint.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (caRoots != null ? caRoots.hashCode() : 0);
        result = 31 * result + (verifyTls != null ? verifyTls.hashCode() : 0);
        return result;
    }
}
