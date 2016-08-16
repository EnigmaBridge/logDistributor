package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Simple domain mapping.
 * Later list of hosts can be added / associated.
 *
 * Created by dusanklinec on 11.08.16.
 */
@Entity
@Table
public class Domain implements DBID {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    @Column
    private String domain;

    public Domain() {
    }

    public Domain(String domain) {
        this.domain = domain;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
