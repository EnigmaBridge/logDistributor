package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by dusanklinec on 03.08.16.
 */
@Entity
@Table(indexes = {
    @Index(columnList = "apiKey")
})
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserObject that = (UserObject) o;

        if (apiKey != null ? !apiKey.equals(that.apiKey) : that.apiKey != null) return false;
        if (uoType != null ? !uoType.equals(that.uoType) : that.uoType != null) return false;
        return uoId != null ? uoId.equals(that.uoId) : that.uoId == null;

    }

    @Override
    public int hashCode() {
        int result = apiKey != null ? apiKey.hashCode() : 0;
        result = 31 * result + (uoType != null ? uoType.hashCode() : 0);
        result = 31 * result + (uoId != null ? uoId.hashCode() : 0);
        return result;
    }
}
