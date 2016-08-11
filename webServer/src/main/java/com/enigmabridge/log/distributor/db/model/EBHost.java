package com.enigmabridge.log.distributor.db.model;

import javax.persistence.*;

/**
 * Enigma bridge host.
 * e.g., site1.enigmabridge.com
 * Created by dusanklinec on 11.08.16.
 */
@Entity
@Table
public class EBHost {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String hostName;

    @Column
    private String hostAddress;

    @ManyToOne(optional = false)
    @JoinColumn(name="domain_id", nullable=false)
    private Domain domain;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }
}
