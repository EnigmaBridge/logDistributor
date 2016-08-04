package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by dusanklinec on 03.08.16.
 */
@Entity
@Table()
public class UserObject {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    @Column
    private String apiKey;

    @Column
    private Integer uoType;

    @Column
    private Integer uoId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Integer getUoType() {
        return uoType;
    }

    public void setUoType(Integer uoType) {
        this.uoType = uoType;
    }

    public Integer getUoId() {
        return uoId;
    }

    public void setUoId(Integer uoId) {
        this.uoId = uoId;
    }
}
