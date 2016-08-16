package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;

/**
 * Created by dusanklinec on 04.08.16.
 */
@Entity
@Table()
public class LogstashConfig implements DBID {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
