package com.enigmabridge.log.distributor.db.dao;

import com.enigmabridge.log.distributor.db.model.EBHost;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 */
public interface EBHostDao extends CrudRepository<EBHost, Integer> {

    List<EBHost> findByDomain(String Domain);

    EBHost deleteByHostName(String HostName);
}
