package com.enigmabridge.log.distributor.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by dusanklinec on 03.08.16.
 */
@Entity
@Table()
public class Client {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    private String clientId;

    @Column
    @OneToMany
    private List<UserObject> objects;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<UserObject> getObjects() {
        return objects;
    }

    public void setObjects(List<UserObject> objects) {
        this.objects = objects;
    }
}
